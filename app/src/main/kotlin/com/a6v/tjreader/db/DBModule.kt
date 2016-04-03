package com.a6v.tjreader.db

import com.a6v.tjreader.App
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DBModule {
  @Provides
  @Singleton
  fun provideImagesDao(db: DBProvider): ImagesDao {
    return ImagesDao(db)
  }

  @Provides
  @Singleton
  fun provideArticlesDao(db: DBProvider): ArticlesDAO {
    return ArticlesDAO(db)
  }

  @Provides
  @Singleton
  fun provideDB(app: App): DBProvider {
    return DBProvider(app, listOf(ArticlesDAO.create(), ImagesDao.create()))
  }
}
