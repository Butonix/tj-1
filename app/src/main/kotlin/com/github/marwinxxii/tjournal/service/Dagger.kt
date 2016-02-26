package com.github.marwinxxii.tjournal.service

import com.github.marwinxxii.tjournal.network.TJournalAPI
import com.squareup.okhttp.OkHttpClient
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
  fun provideService(api: TJournalAPI, cache: ArticlesDAO, downloader: ArticleDownloadService): ArticlesService {
    return ArticlesService(api, cache, downloader)
  }

  @Provides
  @Singleton
  fun provideDownloader(httpClient: OkHttpClient): ArticleDownloadService {
    return ArticleDownloadService(httpClient)
  }

  @Provides
  @Singleton
  fun provideDao(db: DBService): ArticlesDAO {
    return ArticlesDAO(db)
  }
}