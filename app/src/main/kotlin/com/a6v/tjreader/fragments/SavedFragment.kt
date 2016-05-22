package com.a6v.tjreader.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a6v.tjreader.R
import com.a6v.tjreader.activities.MainActivity
import com.a6v.tjreader.activities.ReadActivity
import com.a6v.tjreader.entities.ArticlePreview
import com.a6v.tjreader.service.ArticlesService
import com.a6v.tjreader.utils.ObservableList
import com.a6v.tjreader.widgets.ArticlesAdapter
import com.a6v.tjreader.widgets.PermanentImagePresenter
import com.a6v.tjreader.widgets.ReadButtonController
import kotlinx.android.synthetic.main.widget_article_list.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class SavedFragment : BaseFragment() {
  @Inject lateinit var service: ArticlesService
  @Inject lateinit var imageInteractor: PermanentImagePresenter
  private val articles = ObservableList<ArticlePreview>()

  override fun injectSelf() {
    (activity as MainActivity).component.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.fragment_article_list_read, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    setTitle(0)
    article_list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    article_list.adapter = ArticlesAdapter(articles, imageInteractor, onArticleClick = {
      activity.startActivity(ReadActivity.getIntent(activity, it.id))
    })
    ReadButtonController.run(service, this)
  }

  override fun onResume() {
    super.onResume()
    service.getSavedPreviews()//TODO notify about changes
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle<List<ArticlePreview>>())
      .subscribe({
        articles.set(it)
        //setTitle(it.size)
      })//TODO handle error
  }

  private fun setTitle(count: Int) {
    activity.title = resources.getQuantityString(R.plurals.saved_title, count, count)
  }
}