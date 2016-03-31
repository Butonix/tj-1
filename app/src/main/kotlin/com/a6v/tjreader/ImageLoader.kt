package com.a6v.tjreader

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.nostra13.universalimageloader.cache.disc.DiskCache
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiskCache
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.download.ImageDownloader
import com.nostra13.universalimageloader.utils.IoUtils
import dagger.Module
import dagger.Provides
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import javax.inject.Singleton

class ImageLoaderImpl {
  val loader: ImageLoader

  constructor(context: Context, diskCache: DiskCache) {
    loader = ImageLoader.getInstance()
    loader.init(
      ImageLoaderConfiguration.Builder(context)
        .diskCache(diskCache)
        .imageDownloader(ImageDownloaderImpl(DefaultConfigurationFactory.createImageDownloader(context)))
        .defaultDisplayImageOptions(getDefaultImageOptions().build())
        .writeDebugLogs()
        .build()
    )
  }

  fun displayImage(uri: String, view: ImageView, permanent: Boolean = false) {
    if (permanent) {
      loader.displayImage(toPermanentUri(uri), view)
    } else {
      loader.displayImage(uri, view)
    }
  }

  fun downloadImage(uri: String, permanent: Boolean = false) {
    loader.loadImageSync(if (permanent) toPermanentUri(uri) else uri,
      getDefaultImageOptions().cacheInMemory(false).build())
  }

  fun getDefaultImageOptions(): DisplayImageOptions.Builder {
    return DisplayImageOptions.Builder()
      .cacheInMemory(true)
      .cacheOnDisk(true)
      .resetViewBeforeLoading(true)
  }
}

@Module
class ImageLoaderModule {
  @Provides
  @Singleton
  fun provideImageLoader(context: App, diskCache: CompositeDiskStorage): ImageLoaderImpl {
    return ImageLoaderImpl(context, diskCache)
  }

  @Provides
  @Singleton
  fun provideDiskStorage(context: App): CompositeDiskStorage {
    return CompositeDiskStorage(context, FileNameGeneratorImpl())
  }
}

const val PERMANENT_SCHEME = "permanenthttp"

private fun isPermanent(uri: String): Boolean {
  return uri.startsWith(PERMANENT_SCHEME)
}

private fun toOriginalUri(uri: String): String {
  if (isPermanent(uri)) {
    return uri.replaceFirst(PERMANENT_SCHEME, "http")
  }
  return uri
}

private fun toPermanentUri(uri: String): String {
  if (uri.startsWith("http://") || uri.startsWith("https://")) {
    return uri.replaceFirst("http", PERMANENT_SCHEME)
  }
  return uri
}

class ImageDownloaderImpl(val original: ImageDownloader) : ImageDownloader {
  override fun getStream(uri: String, extra: Any?): InputStream {
    return original.getStream(toOriginalUri(uri), extra)
  }
}

class CompositeDiskStorage : DiskCache {
  //val dir: File
  private val permanentCache: UnlimitedDiskCache
  private val temporaryCache: DiskCache

  constructor(context: Context, fileNameGenerator: FileNameGenerator) {
    //this.dir = dir
    val permanentDir = File(context.filesDir, "permanent")
    permanentDir.mkdir()//FIXME check result
    this.permanentCache = UnlimitedDiskCache(permanentDir, null, fileNameGenerator)
    this.temporaryCache = LimitedAgeDiskCache(context.cacheDir, null, fileNameGenerator, 24 * 60 * 60)//10 Mb
  }

  override fun remove(uri: String): Boolean {
    return delegate(uri, DiskCache::remove)
  }

  override fun save(uri: String, stream: InputStream, cl: IoUtils.CopyListener): Boolean {
    return delegate(uri) { cache, cacheUri ->
      cache.save(cacheUri, stream, cl)
    }
  }

  override fun save(uri: String, bitmap: Bitmap): Boolean {
    return delegate(uri) { cache, cacheUri ->
      cache.save(cacheUri, bitmap)
    }
  }

  override fun clear() {
    //permanentCache.clear()//TODO should cache be cleared?
    temporaryCache.clear()
  }

  override fun close() {
    permanentCache.close()
    temporaryCache.close()
  }

  override fun get(uri: String): File? {
    return delegate(uri, DiskCache::get)
  }

  override fun getDirectory(): File {
    throw UnsupportedOperationException("Can't get root directory for composite storage")
  }

  fun getPermanent(uri: String): File? {
    val file = permanentCache.get(uri)
    return if (file?.exists() ?: false) file else null
  }

  fun copyToPermanent(uri: String) {
    val file = temporaryCache.get(uri)
    if (file != null) {
      file.renameTo(File(permanentCache.directory, file.name))
      //FIXME correct removal from temp
    }
  }

  private inline fun <TResult> delegate(uri: String, func: (DiskCache, String) -> TResult): TResult {
    if (isPermanent(uri)) {
      return func(permanentCache, toOriginalUri(uri))
    } else {
      return func(temporaryCache, uri)
    }
  }
}

class FileNameGeneratorImpl : FileNameGenerator {
  override fun generate(uri: String): String {
    return urlHash(uri)
  }

  fun urlHash(uri: String): String {
    val digest = MessageDigest.getInstance("MD5").digest(uri.toByteArray(charset("UTF-8")))
    return digestToString(digest)
  }

  fun digestToString(bytes: ByteArray): String {
    val hexString = StringBuffer(32)
    for (i in bytes.indices) {
      val hex = Integer.toHexString(bytes[i].toInt().and(0xff))
      if (hex.length == 1) hexString.append('0')
      hexString.append(hex);
    }
    return hexString.toString()
  }
}