package com.a6v.tjreader.activities

import com.a6v.tjreader.entities.ArticlePreview
import com.a6v.tjreader.service.ArticlesService
import com.a6v.tjreader.service.FeedSorting
import com.a6v.tjreader.service.FeedType
import com.a6v.tjreader.utils.ObservableList
import rx.Observable
import rx.Single
import javax.inject.Inject

class FeedManager {
  private val articles: ObservableList<ArticlePreview>
  private val feedType: FeedType
  private val sorting: FeedSorting
  private val service: ArticlesService
  private var articlesObservable: Observable<List<ArticlePreview>>? = null;
  private var currentPage = 0
  private var hasMoreData = true

  @Inject
  constructor(articles: ObservableList<ArticlePreview>,
    feedType: FeedType,
    sorting: FeedSorting,
    service: ArticlesService) {
    this.articles = articles
    this.feedType = feedType
    this.sorting = sorting
    this.service = service
  }

  @Synchronized
  fun loadFirstPage() = loadPage(0)

  @Synchronized
  fun loadNextPage() = loadPage(currentPage + 1)

  @Synchronized
  fun reloadPage() = loadPage(currentPage)

  @Synchronized
  fun hasMoreDataToLoad() = hasMoreData

  @Synchronized
  fun reset() {
    articles.clear()
    //FIXME unsubscribe
  }

  private fun loadPage(page: Int): Single<List<ArticlePreview>> {
    val pageLoading = service.getArticles(page, feedType, sorting)
      .retry(2)
      .doOnNext {
        synchronized(this) {
          currentPage = page
          articles.addAll(it)
          hasMoreData = it.size > 0//TODO paging
        }
      }
      .cache()
    articlesObservable = pageLoading
    return pageLoading.toSingle()
  }
}
