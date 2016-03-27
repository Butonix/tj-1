package com.github.marwinxxii.tjournal.service

import android.database.Cursor
import com.github.marwinxxii.tjournal.entities.*
import com.github.marwinxxii.tjournal.extensions.*
import org.jetbrains.anko.db.*
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.*

class ArticlesDAO(private val db: DBService) {
  private val changesSubject = PublishSubject.create<Any>()

  fun getArticle(id: Int): Observable<Article> {
    return Observable.fromCallable {
      db.getReadable()
        .select("article")
        .where("id=" + id)
        .exec {
          if (!this.moveToFirst()) {
            return@exec null
          }
          Article(readPreview(this), this.getString("text")!!)
        }
    }
      .filterNonNull()
      .subscribeOn(Schedulers.computation())
  }

  fun getArticleSync(id: Int): Article? {
    return db.getReadable()
      .select("article")
      .where("id=" + id)
      .exec {
        if (!this.moveToFirst()) {
          return@exec null
        }
        Article(readPreview(this), this.getString("text")!!)
      }
  }

  fun savePreview(preview: ArticlePreview): Observable<ArticlePreview> {
    return Observable.fromCallable {
      val status = ArticleStatus.WAITING
      val _id = db.getWritable().insert("article",
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
      preview.copy(_id = _id, status = status)
    }.doOnNext { notifyDataChanged() }
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

  private fun readPreview(c: Cursor): ArticlePreview {
    val thumbnail = c.getString("coverThumbnailUrl")
    val fullCover = c.getString("coverUrl")
    val status = c.getInt("status")
    val externalDomain = c.getString("externalDomain")
    val externalUrl = c.getString("externalUrl")
    return ArticlePreview(
      c.getLong("_id"),
      ArticleStatus.values().first { it.ordinal == status },
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

  fun statusToInt(status: ArticleStatus): Int {
    return status.ordinal//TODO
  }
}