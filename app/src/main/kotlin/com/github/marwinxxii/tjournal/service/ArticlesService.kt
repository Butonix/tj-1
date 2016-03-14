package com.github.marwinxxii.tjournal.service

import com.github.marwinxxii.tjournal.CompositeDiskStorage
import com.github.marwinxxii.tjournal.entities.Article
import com.github.marwinxxii.tjournal.entities.ArticlePreview
import com.github.marwinxxii.tjournal.network.TJournalAPI
import org.jsoup.Jsoup
import rx.Observable

/**
 * Created by alexey on 21.02.16.
 */

class ArticlesService(
  private val api: TJournalAPI,
  private val dao: ArticlesDAO,
  private val downloader: ArticleDownloadService,
  private val imageDiskStorage: CompositeDiskStorage) {

  fun getArticles(page: Int): Observable<List<ArticlePreview>> {
    //TODO use deserializer?
    return api.getNews(page * 30)//TODO switch to computation
      .flatMap { previews ->
        dao.getSavedIds(previews.map { it.id })
          .map { savedIds ->
            previews.filter { preview -> !savedIds.contains(preview.id) }
          }
      }
      .map {
        it.map {
          val intro = Jsoup.parse(it.intro).text()
          it.copy(intro = intro)
        }
      }
  }

  fun getArticle(preview: ArticlePreview): Observable<Article> {
    return dao.getArticle(preview.id)
      .switchIfEmpty(
        dao.enqueue(preview)
          .flatMap { saved ->
            downloader.downloadArticleText(saved.url)
              .doOnNext { text -> dao.saveText(saved._id, text) }
              //TODO handle error
              .map { Article(saved, it) }
              .zipWith(
                Observable.defer {
                  if (saved.cover != null) {
                    imageDiskStorage.copyToPermanent(saved.cover.thumbnailUrl)
                  }
                  Observable.just(true)
                },//TODO handle error?

                { article, copiedTrue -> article }
              )
          }
      )
  }

  fun getArticle(id: Int): Article? {
    return dao.getArticleSync(id)
  }

  fun observeArticleCount(): Observable<ArticleCount> {
    return dao.observeArticleChanges().startWith("")
      .flatMap {
        Observable.zip(
          dao.countUnreadArticles(),
          dao.countArticlesByStatus(READY),
          { total, ready -> ArticleCount(total, ready) }
        )
      }
  }

  fun getSavedPreviews(): Observable<List<ArticlePreview>> {
    return dao.observeArticleChanges().startWith("")
      .flatMap { dao.getPreviews("status=" + READY) }
  }
}

data class ArticleCount(val total: Int, val loaded: Int)

const val WAITING = 0
const val LOADING: Int = 1
const val ERROR: Int = 2
const val READY: Int = 3
const val READ: Int = 4