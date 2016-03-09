package com.github.marwinxxii.tjournal.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.marwinxxii.tjournal.R
import com.github.marwinxxii.tjournal.activities.MainActivity
import com.github.marwinxxii.tjournal.entities.ArticlePreview
import com.github.marwinxxii.tjournal.service.ArticlesService
import com.github.marwinxxii.tjournal.widgets.ArticlesAdapter
import com.github.marwinxxii.tjournal.widgets.ReadButtonController
import kotlinx.android.synthetic.main.fragment_article_list_read.*
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Created by alexey on 27.02.16.
 */
class FeedFragment : BaseFragment() {
  @Inject lateinit var service: ArticlesService

  override fun injectSelf() {
    (activity as MainActivity).component.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.fragment_article_list_read, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    setTitle(0)
    article_list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    val adapter = ArticlesAdapter(activity)
    article_list.adapter = adapter
    service.getArticles(0)
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle<List<ArticlePreview>>())
      .subscribe {
        adapter.items.addAll(it)
        adapter.notifyDataSetChanged()
      }

    ReadButtonController.run(service, this)

    ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
      override fun onMove(Rv: RecyclerView, vh: RecyclerView.ViewHolder, vh2: RecyclerView.ViewHolder): Boolean {
        throw UnsupportedOperationException()
      }

      override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
        val position = vh.adapterPosition
        service.getArticle(adapter.items[position]).subscribe()//TODO handler errors
        adapter.items.removeAt(position)
        adapter.notifyItemRemoved(position)
      }
    }).attachToRecyclerView(article_list)
  }

  override fun onDestroyView() {
    //clearFindViewByIdCache()
    super.onDestroyView()
  }

  private fun setTitle(count: Int) {
    if (count > 0) {
      activity.title = getString(R.string.feed) + ' ' + count
    } else {
      activity.title = getString(R.string.feed)
    }
  }
}