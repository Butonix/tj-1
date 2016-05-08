package com.a6v.tjreader.service

import com.a6v.tjreader.db.ArticlesDAO
import com.a6v.tjreader.entities.Article
import com.a6v.tjreader.entities.ArticlePreview
import com.a6v.tjreader.entities.ArticleStatus
import com.a6v.tjreader.network.TJournalAPI
import rx.Observable
import rx.Single

class ArticlesService(
  private val api: TJournalAPI,
  private val dao: ArticlesDAO) {

  fun getArticles(page: Int, type: FeedType, sorting: FeedSorting): Observable<List<ArticlePreview>> {
    //TODO use deserializer?
    val pageSize = 10
    return api.getNews(page * pageSize, pageSize, type.value, sorting2String(sorting))//TODO switch to computation
      .flatMap { previews ->
        dao.getSavedIds(previews.map { it.id })
          .map { savedIds ->
            previews.filter { preview -> !savedIds.contains(preview.id) }
          }
      }
      .map {
        it.map {
          val intro = it.intro
          it.copy(intro = ArticleHtmlParser.getIntroText(intro), introHtml = intro)
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

  fun sorting2String(sorting: FeedSorting): String {
    return when (sorting) {
      FeedSorting.MAIN_PAGE -> "mainpage"
      FeedSorting.RECENT -> "recent"
      FeedSorting.WEEK -> "week"
    }
  }
}

data class ArticleCount(val total: Int, val loaded: Int)

enum class FeedType(val value: Int) {
  ALL(0), NEWS(1), OFF_TOPIC(2), VIDEO(3), ARTICLES(4)
}

enum class FeedSorting {
  MAIN_PAGE, RECENT, WEEK
}