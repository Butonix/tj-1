package com.a6v.tjreader.service

import com.a6v.tjreader.entities.Article
import com.a6v.tjreader.entities.ArticlePreview
import com.a6v.tjreader.entities.ArticleStatus
import com.a6v.tjreader.network.TJournalAPI
import rx.Observable
import rx.Single

class ArticlesService(
  private val api: TJournalAPI,
  private val dao: ArticlesDAO) {

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
          it.copy(intro = ArticleHtmlParser.getIntroText(it.intro))
        }
      }
  }

  fun getArticle(id: Int): Single<Article> {
    return dao.getArticle(id)
  }

  fun observeArticleCount(): Observable<ArticleCount> {
    return dao.observeArticleChanges().startWith("")
      .flatMap {
        Observable.zip(
          dao.countUnreadArticles(),
          dao.countArticlesByStatus(ArticleStatus.READY),
          { total, ready -> ArticleCount(total, ready) }
        )
      }
  }

  fun getSavedPreviews(): Observable<List<ArticlePreview>> {
    return dao.observeArticleChanges().startWith("")
      .flatMap { dao.getPreviews("status=" + ArticlesDAO.statusToInt(ArticleStatus.READY)) }
  }
}

data class ArticleCount(val total: Int, val loaded: Int)
