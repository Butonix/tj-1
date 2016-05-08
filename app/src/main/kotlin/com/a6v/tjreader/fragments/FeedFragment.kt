package com.a6v.tjreader.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a6v.tjreader.EventBus
import com.a6v.tjreader.R
import com.a6v.tjreader.activities.MainActivity
import com.a6v.tjreader.activities.ActivityRetainedState
import com.a6v.tjreader.activities.FeedProvider
import com.a6v.tjreader.entities.Article
import com.a6v.tjreader.entities.ArticlePreview
import com.a6v.tjreader.extensions.subscribeWith
import com.a6v.tjreader.service.ArticleDownloader
import com.a6v.tjreader.service.ArticlesService
import com.a6v.tjreader.service.FeedSorting
import com.a6v.tjreader.service.FeedType
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
import java.util.*
import java.util.concurrent.CancellationException
import javax.inject.Inject

class FeedFragment : BaseFragment {
  @Inject lateinit var service: ArticlesService
  @Inject lateinit var imagePresenter: TempImagePresenter
  @Inject lateinit var articleDownloader: ArticleDownloader
  @Inject lateinit var eventBus: EventBus
  @Inject lateinit var retainedState: ActivityRetainedState
  lateinit var feedProvider: FeedProvider
  lateinit var itemsAdapter: ArticlesAdapter
  lateinit var loadingAdapter: ProgressLoadingAdapter<ArticleViewHolder>

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
    itemsAdapter = ArticlesAdapter(imagePresenter, eventBus)
    val feedType = arguments.getSerializable(ARG_FEED_TYPE) as FeedType
    val sorting = arguments.getSerializable(ARG_SORTING) as FeedSorting
    feedProvider = retainedState.getOrCreate(STATE_FEED_INTERACTOR) {
      FeedProvider(feedType, sorting, service)
    }

    loadingAdapter = ProgressLoadingAdapter(itemsAdapter, { loadNextPage() })
    article_list.adapter = loadingAdapter

    feed_swipe.setOnRefreshListener {
      showRefreshProgress(true)
      feedProvider.reset()
      loadingAdapter.enableNextPageLoading(false)
      itemsAdapter.setItems(Collections.emptyList())
      loadArticles()
    }

    ReadButtonController.run(service, this)

    ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
      override fun onMove(Rv: RecyclerView, vh: RecyclerView.ViewHolder, vh2: RecyclerView.ViewHolder): Boolean {
        throw UnsupportedOperationException()
      }

      override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
        val position = vh.adapterPosition
        articleDownloader.downloadArticle(itemsAdapter.getItem(position))
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
        feedProvider.removeArticleAtPosition(position)
        itemsAdapter.removeItemAt(position)
      }
    }).attachToRecyclerView(article_list)

    articleDownloader.downloadPendingArticles().subscribe()//FIXME handle errors

    if (savedInstanceState == null) {
      showRefreshProgress(true)
    } else {
      showRefreshProgress(savedInstanceState.getBoolean("isRefreshing"))
    }
    loadArticles()
  }

  private fun showRefreshProgress(show: Boolean) {
    //because indicator is not shown right after view created
    feed_swipe.post {
      feed_swipe.isRefreshing = show
    }
  }

  private fun loadArticles() = loadArticles(feedProvider.getArticles())

  private fun loadNextPage() = loadArticles(feedProvider.loadNextPage())

  private fun loadArticles(articles: Single<List<ArticlePreview>>) {
    articles
      .observeOn(AndroidSchedulers.mainThread())
      //TODO shorten
      .compose(RxLifecycle.bindFragment<List<ArticlePreview>>(lifecycle()).forSingle())
      .subscribeWith {
        onSuccess {
          feed_swipe.isRefreshing = false
          if (it.isEmpty()) {
            loadingAdapter.showMessage("Empty")
          } else {
            loadingAdapter.setLoaded(feedProvider.hasMoreDataToLoad())
            itemsAdapter.setItems(it)
          }
        }
        onError {
          feed_swipe.isRefreshing = false
          if (it !is CancellationException) {
            if (isNetworkError(it)) {
              loadingAdapter.showMessage(getString(R.string.feed_error_net))
            } else {
              logError(it)
              loadingAdapter.showMessage(getString(R.string.feed_error_other))
            }
          }
        }
      }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean("isRefreshing", feed_swipe.isRefreshing)//TODO check api < 19
    retainedState.putIfNotPresent(STATE_FEED_INTERACTOR, feedProvider)
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