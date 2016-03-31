package com.a6v.tjreader.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a6v.tjreader.CompositeDiskStorage
import com.a6v.tjreader.EventBus
import com.a6v.tjreader.R
import com.a6v.tjreader.activities.ReadActivity
import com.a6v.tjreader.service.ArticlesService
import com.a6v.tjreader.widgets.ArticleWebViewController
import kotlinx.android.synthetic.main.fragment_article.*
import javax.inject.Inject

/**
 * Created by alexey on 28.02.16.
 */
class ArticleFragment : BaseFragment() {
  @Inject lateinit var eventBus: EventBus
  @Inject lateinit var service: ArticlesService
  @Inject lateinit var imageCache: CompositeDiskStorage
  lateinit var webViewController: ArticleWebViewController

  override fun injectSelf() {
    (activity as ReadActivity).component.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.fragment_article, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    webViewController = ArticleWebViewController(webView, imageCache, service, eventBus)
    //TODO BIND lifecycle
    eventBus.observe(LoadArticleRequestEvent::class.java)
      .compose(bindToLifecycle<LoadArticleRequestEvent>())
      .subscribe { webViewController.loadArticle(it.id) }
    if (arguments != null && arguments.containsKey(KEY_ID)) {
      webViewController.loadArticle(arguments.getInt(KEY_ID))
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
  }

  companion object {
    const val KEY_ID: String = "ID"

    fun createForArticle(id: Int): ArticleFragment {
      val fragment = ArticleFragment()
      fragment.arguments = Bundle()
      fragment.arguments.putInt(KEY_ID, id)
      return fragment
    }
  }
}

data class LoadArticleRequestEvent(val id: Int)