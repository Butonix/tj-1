package com.a6v.tjreader.service

import com.a6v.tjreader.CompositeDiskStorage
import com.a6v.tjreader.ImageLoaderImpl
import com.a6v.tjreader.db.ImagesDao
import com.a6v.tjreader.network.HtmlDownloader
import com.a6v.tjreader.network.TJournalAPI
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by alexey on 22.02.16.
 */

@Module
class ArticlesModule {
  @Provides
  @Singleton
  fun provideService(api: TJournalAPI, cache: ArticlesDAO,
    downloader: HtmlDownloader,
    imageDiskStorage: CompositeDiskStorage,
    imageLoader: ImageLoaderImpl): ArticlesService {
    return ArticlesService(api, cache, downloader, imageDiskStorage, imageLoader)
  }

  @Provides
  @Singleton
  fun provideArticleDownloader(
    articlesDAO: ArticlesDAO,
    imagesDao: ImagesDao,
    htmlDownloader: HtmlDownloader,
    imageLoader: ImageLoaderImpl,
    imageDiskStorage: CompositeDiskStorage
  ): ArticleDownloader {
    return ArticleDownloader(articlesDAO, imagesDao, htmlDownloader, imageLoader, imageDiskStorage)
  }
}