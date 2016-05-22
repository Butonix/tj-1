package com.a6v.tjreader.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a6v.tjreader.R
import com.a6v.tjreader.activities.ActivityRetainedState
import com.a6v.tjreader.activities.FeedManager
import com.a6v.tjreader.activities.MainActivity
import com.a6v.tjreader.entities.Article
import com.a6v.tjreader.entities.ArticlePreview
import com.a6v.tjreader.extensions.subscribe_
import com.a6v.tjreader.service.ArticleDownloader
import com.a6v.tjreader.service.ArticlesService
import com.a6v.tjreader.service.FeedSorting
import com.a6v.tjreader.service.FeedType
import com.a6v.tjreader.utils.ObservableList
import com.a6v.tjreader.utils.isNetworkError
import com.a6v.tjreader.utils.logError
import com.a6v.tjreader.widgets.*
import com.trello.rxlifecycle.RxLifecycle
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.widget_article_list.*
import org.jetbrains.anko.toast
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.subscribeWith
import java.util.concurrent.CancellationException
import javax.inject.Inject

class FeedFragment : BaseFragment {
  @Inject lateinit var service: ArticlesService
  @Inject lateinit var imagePresenter: TempImagePresenter
  @Inject lateinit var articleDownloader: ArticleDownloader
  @Inject lateinit var retainedState: ActivityRetainedState
  lateinit var articles: ObservableList<ArticlePreview>
  lateinit var feedManager: FeedManager
  lateinit var pagingAdapter: PagingAdapter<ArticleViewHolder>

  constructor() {
  }

  override fun injectSelf() {
    (activity as MainActivity).component.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.fragment_feed, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    setTitle(0)
    article_list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

    articles = retainedState.getOrCreate(STATE_ARTICLES) { ObservableList<ArticlePreview>() }
    //FIXME lifecycle bound dataSource
    val feedType = arguments.getSerializable(ARG_FEED_TYPE) as FeedType
    val sorting = arguments.getSerializable(ARG_SORTING) as FeedSorting
    feedManager = retainedState.getOrCreate(STATE_FEED_INTERACTOR) {
      FeedManager(articles, feedType, sorting, service)
    }

    val itemsAdapter = ArticlesAdapter(articles, imagePresenter, {})
    pagingAdapter = PagingAdapter(itemsAdapter,
      onLoadPage = { loadPage(feedManager.loadNextPage()) },
      onReloadPage = { loadPage(feedManager.reloadPage()) }
    )
    article_list.adapter = pagingAdapter

    feed_swipe.setOnRefreshListener {
      showRefreshProgress(true)
      feedManager.reset()
      pagingAdapter.enableNextPageLoading(false)
      loadArticles()
    }

    ReadButtonController.run(service, this)

    ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
      override fun onMove(Rv: RecyclerView, vh: RecyclerView.ViewHolder, vh2: RecyclerView.ViewHolder): Boolean {
        throw UnsupportedOperationException()
      }

      override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
        val position = vh.adapterPosition
        articleDownloader.downloadArticle(articles.get(position))
          .observeOn(AndroidSchedulers.mainThread())
          .toObservable()
          .cache()
          .compose(bindToLifecycle<Article>())
          .subscribeWith {
            onError {
              logError(it)
              activity.toast("Could not load article")
            }
          }
        articles.removeAt(position)
      }
    }).attachToRecyclerView(article_list)

    articleDownloader.downloadPendingArticles().subscribe()//FIXME handle errors

    if (savedInstanceState == null) {
      showRefreshProgress(true)
      loadArticles()
    } else {
      showRefreshProgress(savedInstanceState.getBoolean("isRefreshing"))
    }
  }

  private fun showRefreshProgress(show: Boolean) {
    //because indicator is not shown right after view created
    feed_swipe.post { feed_swipe.isRefreshing = show }
  }

  private fun loadArticles() = loadPage(feedManager.loadFirstPage())

  private fun loadPage(articles: Single<List<ArticlePreview>>) {
    articles
      .observeOn(AndroidSchedulers.mainThread())
      //TODO shorten
      .compose(RxLifecycle.bindFragment<List<ArticlePreview>>(lifecycle()).forSingle())
      .subscribe_(
        onSuccess = {
          showRefreshProgress(false)
          if (it.isEmpty()) {
            pagingAdapter.showMessage("Empty")
          } else {
            pagingAdapter.setLoaded(feedManager.hasMoreDataToLoad())
          }
        },
        onError = {
          showRefreshProgress(false)
          if (it !is CancellationException) {
            if (isNetworkError(it)) {
              pagingAdapter.showMessage(getString(R.string.feed_error_net))
            } else {
              logError(it)
              pagingAdapter.showMessage(getString(R.string.feed_error_other))
            }
          }
        }
      )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean("isRefreshing", feed_swipe.isRefreshing)//TODO check api < 19
    retainedState.putIfNotPresent(STATE_FEED_INTERACTOR, feedManager)
    retainedState.putIfNotPresent(STATE_ARTICLES, articles)
  }

  private fun setTitle(count: Int) {
    if (count > 0) {
      activity.title = getString(R.string.feed) + ' ' + count
    } else {
      activity.title = getString(R.string.feed)
    }
  }

  companion object {
    const val ARG_FEED_TYPE = "feedType"
    const val ARG_SORTING = "sorting"
    private const val STATE_FEED_INTERACTOR = "feedInteractor"
    private const val STATE_ARTICLES = "articles"

    fun create(feedType: FeedType, sorting: FeedSorting): FeedFragment {
      val fragment = FeedFragment()
      val args = Bundle()
      args.putSerializable(ARG_FEED_TYPE, feedType)
      args.putSerializable(ARG_SORTING, sorting)
      fragment.arguments = args
      return fragment
    }
  }
}