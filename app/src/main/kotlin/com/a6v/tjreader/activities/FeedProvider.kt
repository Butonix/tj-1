package com.a6v.tjreader.activities

import com.a6v.tjreader.entities.ArticlePreview
import com.a6v.tjreader.service.ArticlesService
import com.a6v.tjreader.service.FeedSorting
import com.a6v.tjreader.service.FeedType
import rx.Observable
import rx.Single
import java.util.*
import javax.inject.Inject

class FeedProvider {
  private val feedType: FeedType
  private val sorting: FeedSorting
  private val service: ArticlesService
  private val articles = mutableListOf<ArticlePreview>()
  private var firstLoad = true
  private var articlesObservable: Observable<List<ArticlePreview>>? = null;
  private var currentPage = 0
  private var hasMoreData = true

  @Inject
  constructor(feedType: FeedType, sorting: FeedSorting, service: ArticlesService) {
    this.feedType = feedType
    this.sorting = sorting
    this.service = service
  }

  @Synchronized
  fun getArticles(): Single<List<ArticlePreview>> {
    val pageLoading = articlesObservable
    if (pageLoading == null) {
      if (firstLoad) {
        return loadPage(0)
      }
      return Single.just(articles)
    }
    return pageLoading.toSingle()
  }

  @Synchronized
  fun reset() {
    articlesObservable = null
    firstLoad = true
    articles.clear()
  }

  @Synchronized
  fun loadNextPage() = loadPage(currentPage + 1)

  @Synchronized
  fun removeArticleAtPosition(position: Int) {
    articles.removeAt(position)
  }

  @Synchronized
  fun hasMoreDataToLoad() = hasMoreData

  private fun loadPage(page: Int): Single<List<ArticlePreview>> {
    val pageLoading = service.getArticles(page, feedType, sorting)
      .retry(2)
      .doOnNext {
        synchronized(this) {
          firstLoad = false
          currentPage = page
          articles.addAll(it)
          hasMoreData = it.size > 0//TODO paging
          articlesObservable = null
        }
      }
      .map { Collections.unmodifiableList(articles) }
      .cache()
    articlesObservable = pageLoading
    return pageLoading.toSingle()
  }
}
