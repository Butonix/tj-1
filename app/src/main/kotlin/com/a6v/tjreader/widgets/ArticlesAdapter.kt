package com.a6v.tjreader.widgets

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.a6v.tjreader.ImageLoaderImpl
import com.a6v.tjreader.R
import com.a6v.tjreader.entities.ArticlePreview
import javax.inject.Inject

class ArticlesAdapter(val imagePresenter: ImagePresenter) : RecyclerView.Adapter<ArticleViewHolder>() {
  var inflater: LayoutInflater? = null
  val items: MutableList<ArticlePreview> = mutableListOf()

  override fun getItemCount(): Int {
    return items.size
  }

  override fun onBindViewHolder(viewHolder: ArticleViewHolder, position: Int) {
    viewHolder.bind(items[position])
  }

  override fun onCreateViewHolder(parent: ViewGroup, position: Int): ArticleViewHolder {
    if (inflater == null) {
      inflater = LayoutInflater.from(parent.context)
    }
    val view = inflater!!.inflate(R.layout.widget_article_preview, parent, false)
    return ArticleViewHolder(view, imagePresenter)
  }
}

interface ImagePresenter {
  fun displayImage(url: String, view: ImageView)
}

class TempImagePresenter : ImagePresenter {
  private val loader: ImageLoaderImpl

  @Inject
  constructor(loader: ImageLoaderImpl) {
    this.loader = loader
  }

  override fun displayImage(url: String, view: ImageView) {
    loader.displayImage(url, view)
  }
}

class PermanentImagePresenter : ImagePresenter {
  private val loader: ImageLoaderImpl

  @Inject
  constructor(loader: ImageLoaderImpl) {
    this.loader = loader
  }

  override fun displayImage(url: String, view: ImageView) {
    loader.displayImage(url, view, true)
  }
}