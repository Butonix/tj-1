package com.a6v.tjreader.widgets

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.a6v.tjreader.EventBus
import com.a6v.tjreader.R
import com.a6v.tjreader.entities.ArticlePreview

class ArticlesAdapter(
  private val imagePresenter: ImagePresenter,
  private val eventBus: EventBus
) : RecyclerView.Adapter<ArticleViewHolder>() {
  private val items = mutableListOf<ArticlePreview>()
  private var inflater: LayoutInflater? = null

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
    return ArticleViewHolder(view, imagePresenter, eventBus)
  }

  fun setItems(items: List<ArticlePreview>) {
    this.items.clear()
    this.items.addAll(items)
    notifyDataSetChanged()
  }

  fun getItem(position: Int): ArticlePreview {
    return items[position]
  }

  fun removeItemAt(position: Int) {
    items.removeAt(position)
    notifyItemRemoved(position)
  }
}
