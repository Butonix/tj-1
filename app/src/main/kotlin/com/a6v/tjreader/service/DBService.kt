package com.a6v.tjreader.service

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.a6v.tjreader.App
import dagger.Module
import dagger.Provides
import org.jetbrains.anko.db.*
import javax.inject.Singleton

/**
 * Created by alexey on 22.02.16.
 */

class DBService(app: App) {
  private val helper = DBOpenHelper(app)

  fun getWritable(): SQLiteDatabase {
    return helper.writableDatabase
  }

  fun getReadable(): SQLiteDatabase {
    return helper.readableDatabase
  }

  inner class DBOpenHelper(app: App) : SQLiteOpenHelper(app, "tjournal.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
      db.createTable("article", true,
        "_id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
        "status" to INTEGER,
        "id" to INTEGER + UNIQUE,
        "title" to TEXT,
        "url" to TEXT,
        "intro" to TEXT,
        "date" to INTEGER,
        "commentsCount" to INTEGER,
        "likes" to INTEGER,
        "coverThumbnailUrl" to TEXT,
        "coverUrl" to TEXT,
        "externalDomain" to TEXT,
        "externalUrl" to TEXT,
        "text" to TEXT
      )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
  }
}

@Module
class DBModule {
  @Provides
  @Singleton
  fun provideDB(app: App): DBService {
    return DBService(app)
  }
}
