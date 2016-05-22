package com.a6v.tjreader.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a6v.tjreader.R
import com.a6v.tjreader.activities.MainActivity
import com.a6v.tjreader.db.ArticlesDAO
import com.a6v.tjreader.entities.ArticlePreview
import com.a6v.tjreader.utils.ObservableList
import com.a6v.tjreader.widgets.ArticlesAdapter
import com.a6v.tjreader.widgets.PermanentImagePresenter
import kotlinx.android.synthetic.main.widget_article_list.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class ReadFragment : BaseFragment() {
  @Inject lateinit var dao: ArticlesDAO
  @Inject lateinit var imageInteractor: PermanentImagePresenter

  override fun injectSelf() {
    (activity as MainActivity).component.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.widget_article_list, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    setTitle(0)
    article_list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    val articles = ObservableList<ArticlePreview>()
    val adapter = ArticlesAdapter(articles, imageInteractor, {})
    article_list.adapter = adapter
    dao.getReadArticles()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle<List<ArticlePreview>>())
      .subscribe({
        if (it.isEmpty()) {
        } else {
          articles.set(it)
        }
        setTitle(it.size)
      })//TODO handle error
  }

  private fun setTitle(count: Int) {
    activity.title = resources.getQuantityString(R.plurals.read_title, count, count)
  }
}