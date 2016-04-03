package com.a6v.tjreader.db

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.a6v.tjreader.App
import org.jetbrains.anko.db.*

class DBProvider(app: App, createCallbacks: List<(SQLiteDatabase) -> Unit>) {
  private val helper = DBOpenHelper(app, createCallbacks)

  fun getWritable(): SQLiteDatabase {
    return helper.writableDatabase
  }

  fun getReadable(): SQLiteDatabase {
    return helper.readableDatabase
  }
}

class DBOpenHelper(app: App, private val createCallbacks: List<(SQLiteDatabase) -> Unit>)
: SQLiteOpenHelper(app, "tjournal.db", null, 1) {
  override fun onCreate(db: SQLiteDatabase) {
    for (callback in createCallbacks) {
      db.transaction {
        callback(db)
      }
    }
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
  }
}
