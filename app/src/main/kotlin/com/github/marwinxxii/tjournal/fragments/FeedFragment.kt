package com.github.marwinxxii.tjournal.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.marwinxxii.tjournal.R
import com.github.marwinxxii.tjournal.activities.ReadActivity
import com.github.marwinxxii.tjournal.extensions.startActivityWithClass
import com.github.marwinxxii.tjournal.service.ArticlesService
import com.github.marwinxxii.tjournal.widgets.ArticlesAdapter
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_feed.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by alexey on 27.02.16.
 */
class FeedFragment : Fragment() {
  @Inject lateinit var service: ArticlesService

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent().plus(FragmentModule(this)).inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.fragment_feed, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    setTitle(0)
    items.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    val adapter = ArticlesAdapter(activity)
    items.adapter = adapter
    service.getArticles(0)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe {
        adapter.items.addAll(it)
        adapter.notifyDataSetChanged()
      }

    service.observeArticleCount()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe {
        if (it.total > 0) {
          read.visibility = View.VISIBLE
          read.text = "Read ${it.loaded} / ${it.total}"
        } else {
          read.visibility = View.GONE
        }
      }

    read.setOnClickListener({
      activity.startActivityWithClass(ReadActivity::class.java)
    })

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
    }).attachToRecyclerView(items)
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