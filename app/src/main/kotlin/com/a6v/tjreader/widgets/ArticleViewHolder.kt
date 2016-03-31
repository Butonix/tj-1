package com.a6v.tjreader.widgets

import android.support.v7.widget.RecyclerView
import android.view.View
import com.a6v.tjreader.entities.ArticlePreview
import com.a6v.tjreader.extensions.gone
import com.a6v.tjreader.extensions.toggleVisibility
import com.a6v.tjreader.extensions.toggleVisibilityAndText
import kotlinx.android.synthetic.main.widget_article_preview.view.*

class ArticleViewHolder(itemView: View, val imageLoader: ImagePresenter) : RecyclerView.ViewHolder(itemView) {
  fun bind(article: ArticlePreview) {
    itemView.title.text = article.title
    itemView.intro.text = article.intro
    if (article.cover != null) {
      imageLoader.displayImage(article.cover.thumbnailUrl, itemView.photo)
    } else {
      itemView.photo.gone()
    }
    itemView.comments.toggleVisibilityAndText(article.commentsCount > 0, article.commentsCount)
    itemView.likes.toggleVisibility(article.likes != 0)
    //todo colors
    itemView.likes.text = (if (article.likes > 0) "+" else "-") + article.likes
  }
}