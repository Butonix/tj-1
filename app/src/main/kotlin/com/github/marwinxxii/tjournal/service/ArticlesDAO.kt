package com.github.marwinxxii.tjournal.service

import android.database.Cursor
import com.github.marwinxxii.tjournal.entities.Article
import com.github.marwinxxii.tjournal.entities.ArticlePreview
import com.github.marwinxxii.tjournal.entities.CoverPhoto
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

  fun enqueue(preview: ArticlePreview): Observable<ArticlePreview> {
    return Observable.fromCallable {
      val _id = db.getWritable().insert("article",
        "status" to WAITING,
        "id" to preview.id,
        "title" to preview.title,
        "url" to preview.url,
        "intro" to preview.intro,
        "date" to preview.date.time,
        "commentsCount" to preview.commentsCount,
        "likes" to preview.likes,
        "coverThumbnailUrl" to preview.cover?.thumbnailUrl,
        "coverUrl" to preview.cover?.url
      )
      preview.copy(_id = _id, status = WAITING)
    }.doOnNext { notifyDataChanged() }
  }

  fun saveText(_id: Long, text: String) {
    db.getWritable()
      .update("article", "status" to READY, "text" to text)
      .where("_id=" + _id)
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
        .where("status=" + READY)
        .orderBy("date", SqlOrderDirection.DESC)
        .parseList(rowParser { id: Long, title:String -> Pair(id.toInt(), title) })
    }
  }

  fun observeArticleChanges(): Observable<Any> {
    return changesSubject
  }

  fun countArticlesByStatus(status: Int): Observable<Int> {
    return Observable.fromCallable {
      db.getReadable()
        .select("article", "count(*)")
        .where("status=" + status)
        .parseSingle(IntParser)
    }
  }

  fun countUnreadArticles(): Observable<Int> {
    return Observable.fromCallable {
      db.getReadable()
        .select("article", "count(*)")
        .where("status!=" + READ)
        .parseSingle(IntParser)
    }
  }

  fun markArticleRead(id: Int): Observable<Any> {
    return Observable.fromCallable {
      db.getWritable().update("article", "status" to READ).where("id=" + id).exec()
      notifyDataChanged()
    }
  }

  fun getSavedArticles(): Observable<List<ArticlePreview>> {
    return Observable.fromCallable {
      db.getReadable()
        .select("article", "*")
        .where("status=" + READY)
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
        .where("status=" + READ)
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
    return ArticlePreview(
      c.getLong("_id"),
      c.getInt("status"),
      c.getInt("id"),
      c.getString("title")!!,
      c.getString("url")!!,
      c.getString("intro")!!,
      Date(c.getLong("date")),
      c.getInt("commentsCount"),
      c.getInt("likes"),
      if (thumbnail != null && fullCover != null) CoverPhoto(thumbnail, fullCover) else null
    )
  }
}