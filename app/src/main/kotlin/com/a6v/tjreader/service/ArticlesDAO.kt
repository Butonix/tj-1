package com.a6v.tjreader.service

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.a6v.tjreader.db.DaoInit
import com.a6v.tjreader.entities.*
import com.a6v.tjreader.extensions.*
import org.jetbrains.anko.db.*
import rx.Observable
import rx.Single
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.*

class ArticlesDAO(private val db: DBService) {
  private val changesSubject = PublishSubject.create<Any>()

  fun getArticle(id: Int): Single<Article> {
    return Single.fromCallable {
      db.getReadable()
        .select("article")
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
      db.getWritable().insert(TABLE_NAME,
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
        "externalUrl" to preview.externalLink?.url
      )
      notifyDataChanged()
      preview
    }
  }

  fun saveText(id: Int, text: String) {
    db.getWritable()
      .update("article",
        "status" to statusToInt(ArticleStatus.READY),
        "text" to text
      )
      .where("id=" + id)
      .exec()
    //TODO check exec result
    notifyDataChanged()
  }

  private fun notifyDataChanged() {
    changesSubject.onNext(null)
  }

  fun getReadyArticlesIds(): Observable<List<Pair<Int, String>>> {
    return Observable.fromCallable {
      db.getReadable()
        .select("article", "id", "title")
        .where("status=" + statusToInt(ArticleStatus.READY))
        .orderBy("date", SqlOrderDirection.DESC)
        .parseList(rowParser { id: Long, title:String -> Pair(id.toInt(), title) })
    }
  }

  fun getSavedIds(ids: List<Int>): Observable<List<Int>> {
    //TODO batch ids
    return Observable.fromCallable {
      db.getReadable()
        .select("article", "id")
        .where("status>=" + statusToInt(ArticleStatus.READY) + " AND id in (" + ids.joinToString(",") + ")")
        .parseList(IntParser)
    }
  }

  fun observeArticleChanges(): Observable<Any> {
    return changesSubject
  }

  fun countArticlesByStatus(status: ArticleStatus): Observable<Int> {
    return Observable.fromCallable {
      db.getReadable()
        .select("article", "count(*)")
        .where("status=" + statusToInt(status))
        .parseSingle(IntParser)
    }
  }

  fun countUnreadArticles(): Observable<Int> {
    return Observable.fromCallable {
      db.getReadable()
        .select("article", "count(*)")
        .where("status!=" + statusToInt(ArticleStatus.READ))
        .parseSingle(IntParser)
    }
  }

  fun markArticleRead(id: Int): Observable<Any> {
    return Observable.fromCallable {
      db.getWritable().update("article", "status" to statusToInt(ArticleStatus.READ)).where("id=" + id).exec()
      notifyDataChanged()
    }
  }

  fun getPreviews(where: String): Observable<List<ArticlePreview>> {
    return Observable.fromCallable {
      db.getReadable()
        .select("article", "*")
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
      db.getReadable()
        .select("article", "*")
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
    return Observable.empty()
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
      if (externalUrl != null && externalDomain != null) ArticleExternalSource(externalDomain, externalUrl) else null
    )
  }

  companion object {
    const val TABLE_NAME = "article"//TODO rename later

    fun statusToInt(status: ArticleStatus): Int {
      return status.ordinal//TODO
    }
  }
}

class ArticlesDaoInit: DaoInit {
  override fun onCreate(db: SQLiteDatabase) {
    db.createTable(ArticlesDAO.TABLE_NAME, true,
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

}