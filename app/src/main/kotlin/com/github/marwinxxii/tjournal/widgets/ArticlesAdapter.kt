package com.github.marwinxxii.tjournal.widgets

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.marwinxxii.tjournal.EventBus
import com.github.marwinxxii.tjournal.R
import com.github.marwinxxii.tjournal.entities.ArticlePreview
import com.github.marwinxxii.tjournal.widgets.ArticleViewHolder

class ArticlesAdapter(context: Context) : RecyclerView.Adapter<ArticleViewHolder>() {
  val inflater: LayoutInflater = LayoutInflater.from(context)
  val items: MutableList<ArticlePreview> = mutableListOf()

  override fun getItemCount(): Int {
    return items.size
  }

  override fun onBindViewHolder(viewHolder: ArticleViewHolder, position: Int) {
    viewHolder.bind(items[position])
  }

  override fun onCreateViewHolder(parent: ViewGroup, position: Int): ArticleViewHolder {
    val view = inflater.inflate(R.layout.widget_article_preview, parent, false)
    return ArticleViewHolder(view, EventBus())
  }
}