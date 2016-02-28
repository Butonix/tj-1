package com.github.marwinxxii.tjournal.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.marwinxxii.tjournal.EventBus
import com.github.marwinxxii.tjournal.R
import com.github.marwinxxii.tjournal.service.ArticlesDAO
import kotlinx.android.synthetic.main.fragment_article.*
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Created by alexey on 28.02.16.
 */
class ArticleFragment : Fragment() {
  @Inject lateinit var eventBus: EventBus
  @Inject lateinit var dao: ArticlesDAO

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent().plus(FragmentModule(this)).inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.fragment_article, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    webView.visibility
    //TODO BIND lifecycle
    eventBus.observe(LoadArticleRequestEvent::class.java).subscribe {
      loadArticle(it.id)
    }
    if (arguments != null && arguments.containsKey(KEY_ID)) {
      loadArticle(arguments.getInt(KEY_ID))
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
  }

  fun loadArticle(id: Int) {
    dao.getArticle(id)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe {
        activity.title = it.preview.title
        webView.loadData(prepareText(it.text), "text/html; charset=utf-8", "utf-8")
      }
  }

  //TODO move to background thread
  fun prepareText(text: String): String {
    return "<!doctype html><html><head><meta charset=\"utf-8\">" +
      "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
      "<style>article img { max-width: 100%;}</style>" +
      "</head><body>$text</body>"
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