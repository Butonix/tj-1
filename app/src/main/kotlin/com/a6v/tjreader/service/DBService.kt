package com.a6v.tjreader.service

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.a6v.tjreader.App
import com.a6v.tjreader.db.DaoInit
import org.jetbrains.anko.db.*

/**
 * Created by alexey on 22.02.16.
 */

class DBService(app: App, daoIniters: List<DaoInit>) {
  private val helper = DBOpenHelper(app, daoIniters)

  fun getWritable(): SQLiteDatabase {
    return helper.writableDatabase
  }

  fun getReadable(): SQLiteDatabase {
    return helper.readableDatabase
  }
}

class DBOpenHelper(app: App, private val daoIniters: List<DaoInit>)
: SQLiteOpenHelper(app, "tjournal.db", null, 1) {
  override fun onCreate(db: SQLiteDatabase) {
    for (d in daoIniters) {
      db.transaction {
        d.onCreate(db)
      }
    }
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
  }
}
