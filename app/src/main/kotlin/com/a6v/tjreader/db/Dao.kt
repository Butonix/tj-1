package com.a6v.tjreader.db

import android.database.sqlite.SQLiteDatabase

interface DaoInit {
  fun onCreate(db: SQLiteDatabase)
}
