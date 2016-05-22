package com.a6v.tjreader.widgets

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.a6v.tjreader.R
import com.a6v.tjreader.entities.ArticlePreview
import com.a6v.tjreader.utils.DataSourceAdapterObserver
import com.a6v.tjreader.utils.ObservableList
import rx.android.schedulers.AndroidSchedulers

class ArticlesAdapter : RecyclerView.Adapter<ArticleViewHolder> {
  private val articles: ObservableList<ArticlePreview>
  private val imagePresenter: ImagePresenter
  private val onArticleClick: (ArticlePreview) -> Unit
  private var inflater: LayoutInflater? = null

  constructor(
    storage: ObservableList<ArticlePreview>,
    imagePresenter: ImagePresenter,
    onArticleClick: (ArticlePreview) -> Unit)
  {
    this.imagePresenter = imagePresenter
    this.onArticleClick = onArticleClick
    this.articles = storage
    storage.observeData()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(DataSourceAdapterObserver(this))
  }

  override fun getItemCount(): Int = articles.size()

  override fun onBindViewHolder(viewHolder: ArticleViewHolder, position: Int) {
    viewHolder.bind(articles.get(position))
  }

  override fun onCreateViewHolder(parent: ViewGroup, position: Int): ArticleViewHolder {
    if (inflater == null) {
      inflater = LayoutInflater.from(parent.context)
    }
    val view = inflater!!.inflate(R.layout.widget_article_preview, parent, false)
    return ArticleViewHolder(view, imagePresenter, onArticleClick)
  }
}
