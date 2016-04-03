package com.a6v.tjreader.service

import com.a6v.tjreader.CompositeDiskStorage
import com.a6v.tjreader.ImageLoaderImpl
import com.a6v.tjreader.db.ImageState
import com.a6v.tjreader.db.ImagesDao
import com.a6v.tjreader.entities.ArticlePreview
import com.a6v.tjreader.extensions.flatMapCompletable
import rx.Completable

class ImageDownloader(
  private val dao: ImagesDao,
  private val loaderImpl: ImageLoaderImpl,
  private val diskStorage: CompositeDiskStorage
) {
  fun saveThumbnail(saved: ArticlePreview, thumbnail: String): Completable {
    if (diskStorage.tempExists(thumbnail)) {
      return Completable.fromAction {
        diskStorage.copyToPermanent(thumbnail)//TODO file can be deleted
      }.endWith(
        Completable.fromSingle(dao.saveImage(saved.id, thumbnail, ImageState.LOADED))
      )
    } else {
      return saveImageAndLoad(saved.id, thumbnail)
    }
  }

  fun saveImageAndLoad(articleId: Int, url: String): Completable {
    return dao.saveImage(articleId, url, ImageState.NOT_LOADED)
      .flatMapCompletable { downloadAndUpdateImage(it, url) }
  }

  private fun downloadAndUpdateImage(_id: Long, url: String): Completable {
    return Completable.fromAction { loaderImpl.downloadImage(url, true) }
      .retry(2)
      .endWith(dao.updateImage(_id, ImageState.LOADED))
  }
}
