package com.a6v.tjreader.db

import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import com.a6v.tjreader.extensions.where
import org.jetbrains.anko.db.*
import rx.Completable
import rx.Observable
import rx.Single

class ImagesDao(private val db: DBProvider) {
  fun saveImage(articleId: Int, imageUrl: String, state: ImageState): Single<Long> {
    return Single.fromCallable {
      db.getWritable().insert(TABLE_NAME,
        ARTICLE_ID to articleId,
        URL to imageUrl,
        STATE to state2Int(state)
      )
    }
  }

  fun saveImages(articleId: Int, images: List<String>, state: ImageState): Single<List<Long>> {
    return Single.fromCallable {
      val ids = mutableListOf<Long>()
      db.getWritable().transaction {
        for (i in images) {
          val _id = this.insert(TABLE_NAME,
            ARTICLE_ID to articleId,
            URL to i,
            STATE to state2Int(state)
          )
          ids.add(_id)
        }
      }
      ids//FIXME check result
    }
  }

  fun queryImages(articleId: Int, state: ImageState): Observable<String> {
    return queryImages(ARTICLE_ID to articleId, STATE to state2Int(state))
  }

  fun queryImages(state: ImageState): Observable<String> {
    return queryImages(STATE to state2Int(state))
  }

  private fun queryImages(vararg where: Pair<String, Any>): Observable<String> {
    return Observable.create { subscriber ->
      db.getReadable()
        .select(TABLE_NAME, URL)
        .where(*where)
        .exec {
          while (this.moveToNext()) {
            if (!subscriber.isUnsubscribed) {
              subscriber.onNext(this.getString(0))
            } else {
              break
            }
          }
          if (!subscriber.isUnsubscribed) {
            subscriber.onCompleted()
          }
        }
    }
  }

  fun updateImage(_id: Long, state: ImageState): Completable {
    return Completable.fromAction {
      db.getWritable()
        .update(TABLE_NAME, STATE to state2Int(state))
        .where(BaseColumns._ID to _id)
        .exec()//TODO check result
    }
  }

  companion object {
    const val TABLE_NAME = "images"
    const val ARTICLE_ID = "articleId"
    const val URL = "url"
    const val STATE = "state"

    fun state2Int(state: ImageState): Int {
      return state.ordinal
    }
  }
}

class ImagesDaoInit : DaoInit {
  override fun onCreate(db: SQLiteDatabase) {
    db.createTable(ImagesDao.TABLE_NAME, true,
      BaseColumns._ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
      ImagesDao.ARTICLE_ID to INTEGER,
      ImagesDao.URL to TEXT, //FIXME constraint id + url unique
      ImagesDao.STATE to INTEGER
    )
  }
}

enum class ImageState {
  NOT_LOADED, LOADED, ERROR
}