package com.a6v.tjreader.db

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import com.a6v.tjreader.entities.*
import com.a6v.tjreader.extensions.getBoolean
import com.a6v.tjreader.extensions.getInt
import com.a6v.tjreader.extensions.getLong
import com.a6v.tjreader.extensions.getString
import org.jetbrains.anko.db.*
import rx.Completable
import rx.Observable
import rx.Single
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.*

class ArticlesDAO(db: DBProvider) : BaseDao(NAME, db) {
  private val changesSubject = PublishSubject.create<Any>()

  fun getArticle(id: Int): Single<Article> {
    return Single.fromCallable {
      select()
        .where("id=" + id)
        .exec {
          if (!this.moveToFirst()) {
            throw NullPointerException("Article with $id not found")
          }
          Article(readPreview(this), this.getString("text")!!)
        }
    }
      .subscribeOn(Schedulers.computation())
  }

  fun savePreview(preview: ArticlePreview, status: ArticleStatus): Single<ArticlePreview> {
    return Single.fromCallable {
      insert(
        "status" to statusToInt(status),
        "id" to preview.id,
        "title" to preview.title,
        "url" to preview.url,
        "intro" to preview.intro,
        "date" to preview.date.time,
        "commentsCount" to preview.commentsCount,
        "likes" to preview.likes,
        "coverThumbnailUrl" to preview.cover?.thumbnailUrl,
        "coverUrl" to preview.cover?.url,
        "externalDomain" to preview.externalLink?.domain,
        "externalUrl" to preview.externalLink?.url,
        HAS_FULL_TEXT to preview.hasFullText
      )
      notifyDataChanged()
      preview
    }
  }

  fun saveArticle(article: Article, status: ArticleStatus): Completable {
    return Completable.fromAction {
      db.getWritable().transaction {
        //TODO perform this in a single action
        Completable.concat(
          Completable.fromSingle(savePreview(article.preview, status)),
          Completable.fromSingle(updateArticle(article.preview, article.text))
        ).await()
      }
    }
  }

  fun updateArticle(preview: ArticlePreview, text: String): Single<Article> {
    return Single.fromCallable {
      update("text" to text).where("id=" + preview.id).exec()
      //TODO check exec result
      notifyDataChanged()
      Article(preview, text)
    }
  }

  fun updateArticle(id: Int, status: ArticleStatus): Completable {
    return Completable.fromAction {
      update("status" to statusToInt(status))
        .where("id=" + id)
        .exec()
      //TODO check exec result
      notifyDataChanged()
    }
  }

  private fun notifyDataChanged() {
    changesSubject.onNext(null)
  }

  fun getReadyArticlesIds(): Observable<List<Pair<Int, String>>> {
    return Observable.fromCallable {
      select("id", "title")
        .where("status=" + statusToInt(ArticleStatus.READY))
        .orderBy("date", SqlOrderDirection.DESC)
        .parseList(rowParser { id: Long, title: String -> Pair(id.toInt(), title) })
    }
  }

  fun getSavedIds(ids: List<Int>): Observable<List<Int>> {
    //TODO batch ids
    return Observable.fromCallable {
      select("id")
        .where("status>=" + statusToInt(ArticleStatus.READY) + " AND id in (" + ids.joinToString(",") + ")")
        .parseList(IntParser)
    }
  }

  fun observeArticleChanges(): Observable<Any> {
    return changesSubject
  }

  fun countArticlesByStatus(status: ArticleStatus): Observable<Int> {
    return Observable.fromCallable {
      selectCount()
        .where("status=" + statusToInt(status))
        .parseSingle(IntParser)
    }
  }

  fun countUnreadArticles(): Observable<Int> {
    return Observable.fromCallable {
      selectCount()
        .where("status!=" + statusToInt(ArticleStatus.READ))
        .parseSingle(IntParser)
    }
  }

  fun markArticleRead(id: Int): Observable<Any> {
    return Observable.fromCallable {
      update("status" to statusToInt(ArticleStatus.READ)).where("id=" + id).exec()
      notifyDataChanged()
    }
  }

  fun getPreviews(where: String): Observable<List<ArticlePreview>> {
    return Observable.fromCallable {
      select()
        .where(where)
        .exec {
          val result = mutableListOf<ArticlePreview>()
          while (this.moveToNext()) {
            result.add(readPreview(this))
          }
          result
        }
    }
  }

  fun getReadArticles(): Observable<List<ArticlePreview>> {
    return Observable.fromCallable {
      select()
        .where("status=" + statusToInt(ArticleStatus.READ))
        .exec {
          val result = mutableListOf<ArticlePreview>()
          while (this.moveToNext()) {
            result.add(readPreview(this))
          }
          result
        }
    }
  }

  fun queryArticles(status: ArticleStatus): Observable<ArticlePreview> {
    return Observable.create { subscriber ->
      select()
        .where("status=" + statusToInt(status))
        .exec {
          while (this.moveToNext()) {
            if (!subscriber.isUnsubscribed) {
              subscriber.onNext(readPreview(this))
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

  private fun readPreview(c: Cursor): ArticlePreview {
    val thumbnail = c.getString("coverThumbnailUrl")
    val fullCover = c.getString("coverUrl")
    val externalDomain = c.getString("externalDomain")
    val externalUrl = c.getString("externalUrl")
    return ArticlePreview(
      c.getInt("id"),
      c.getString("title")!!,
      c.getString("url")!!,
      c.getString("intro")!!,
      Date(c.getLong("date")),
      c.getInt("commentsCount"),
      c.getInt("likes"),
      if (thumbnail != null && fullCover != null) CoverPhoto(thumbnail, fullCover) else null,
      if (externalUrl != null && externalDomain != null) ArticleExternalSource(externalDomain, externalUrl) else null,
      c.getBoolean(HAS_FULL_TEXT)
    )
  }

  companion object {
    const val NAME = "articles"
    const val HAS_FULL_TEXT = "hasFullText"

    fun statusToInt(status: ArticleStatus): Int {
      return status.ordinal//TODO
    }

    fun create(): (SQLiteDatabase) -> Unit {
      return {
        it.createTable(NAME, true,
          BaseColumns._ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
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
          "text" to TEXT,
          HAS_FULL_TEXT to INTEGER
        )
      }
    }
  }
}