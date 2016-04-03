package com.a6v.tjreader.db

import com.a6v.tjreader.App
import com.a6v.tjreader.service.ArticlesDAO
import com.a6v.tjreader.service.ArticlesDaoInit
import com.a6v.tjreader.service.DBService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DBModule {
  @Provides
  @Singleton
  fun provideImagesDao(db: DBService): ImagesDao {
    return ImagesDao(db)
  }

  @Provides
  @Singleton
  fun provideArticlesDao(db: DBService): ArticlesDAO {
    return ArticlesDAO(db)
  }

  @Provides
  @Singleton
  fun provideDB(app: App): DBService {
    return DBService(app, listOf(ArticlesDaoInit(), ImagesDaoInit()))
  }
}
