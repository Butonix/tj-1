package com.github.marwinxxii.tjournal.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.marwinxxii.tjournal.R
import com.github.marwinxxii.tjournal.activities.MainActivity
import com.github.marwinxxii.tjournal.entities.ArticlePreview
import com.github.marwinxxii.tjournal.service.ArticlesDAO
import com.github.marwinxxii.tjournal.widgets.ArticlesAdapter
import kotlinx.android.synthetic.main.fragment_article_list.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by alexey on 27.02.16.
 */
class ReadFragment : BaseFragment() {
  @Inject lateinit var dao: ArticlesDAO

  override fun injectSelf() {
    (activity as MainActivity).component.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.fragment_article_list, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    setTitle(0)
    article_list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    val adapter = ArticlesAdapter(activity)
    article_list.adapter = adapter
    dao.getReadArticles()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle<List<ArticlePreview>>())
      .subscribe({
        if (it.isEmpty()) {
        } else {
          adapter.items.addAll(it)
          adapter.notifyDataSetChanged()
        }
        setTitle(it.size)
      })//TODO handle error
  }

  override fun onDestroyView() {
    //clearFindViewByIdCache()
    super.onDestroyView()
  }

  private fun setTitle(count: Int) {
    activity.title = resources.getQuantityString(R.plurals.read_title, count, count)
  }
}