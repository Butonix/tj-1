package com.a6v.tjreader.db

import com.a6v.tjreader.extensions._toContentValues
import org.jetbrains.anko.db.SelectQueryBuilder
import org.jetbrains.anko.db.UpdateQueryBuilder
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update

abstract class BaseDao(private val tableName: String, protected val db: DBProvider) {
  fun select(): SelectQueryBuilder {
    return db.getReadable().select(tableName)
  }

  fun select(vararg columns: String): SelectQueryBuilder {
    return db.getReadable().select(tableName, *columns)
  }

  fun selectCount(): SelectQueryBuilder {
    return db.getReadable().select(tableName, "count(*)")
  }

  fun update(vararg values: Pair<String, Any>): UpdateQueryBuilder {
    return db.getWritable().update(tableName, *values)
  }

  fun insert(vararg values: Pair<String, Any?>): Long {
    return db.getWritable().insert(tableName, null, values._toContentValues())
  }
}
