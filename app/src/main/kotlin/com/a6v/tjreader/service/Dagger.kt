package com.a6v.tjreader.service

import com.a6v.tjreader.CompositeDiskStorage
import com.a6v.tjreader.ImageLoaderImpl
import com.a6v.tjreader.db.ArticlesDAO
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
  fun provideService(api: TJournalAPI, articleDao: ArticlesDAO): ArticlesService {
    return ArticlesService(api, articleDao)
  }

  @Provides
  @Singleton
  fun provideArticleDownloader(
    articlesDAO: ArticlesDAO,
    htmlDownloader: HtmlDownloader,
    imageDownloader: ImageDownloader
  ): ArticleDownloader {
    return ArticleDownloader(articlesDAO,  htmlDownloader, imageDownloader)
  }

  @Provides
  @Singleton
  fun provideImageDownloader(
    imagesDao: ImagesDao,
    imageLoader: ImageLoaderImpl,
    imageDiskStorage: CompositeDiskStorage
  ): ImageDownloader {
    return ImageDownloader(imagesDao, imageLoader, imageDiskStorage)
  }
}