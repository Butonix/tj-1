package com.github.marwinxxii.tjournal.widgets

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.View
import com.github.marwinxxii.tjournal.EventBus
import com.github.marwinxxii.tjournal.entities.ArticlePreview
import com.github.marwinxxii.tjournal.extensions.toggleVisibility
import com.github.marwinxxii.tjournal.extensions.toggleVisibilityAndText
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import kotlinx.android.synthetic.main.widget_article_preview.view.*

class ArticleViewHolder(itemView: View, val eventBus: EventBus) : RecyclerView.ViewHolder(itemView) {
  fun bind(article: ArticlePreview) {
    itemView.title.text = article.title
    itemView.intro.text = article.intro
    if (article.cover != null) {
      ImageLoader.getInstance().displayImage(article.cover.thumbnailUrl, itemView.photo, object: ImageLoadingListener {
        override fun onLoadingStarted(url: String, view: View) {
        }

        override fun onLoadingCancelled(url: String, view: View) {
          view.visibility = View.GONE
        }

        override fun onLoadingComplete(p0: String?, view: View, p2: Bitmap?) {
          view.visibility = View.VISIBLE
        }

        override fun onLoadingFailed(p0: String?, view: View, p2: FailReason?) {
          view.visibility = View.GONE
        }
      })
    }
    itemView.comments.toggleVisibilityAndText(article.commentsCount > 0, article.commentsCount)
    itemView.likes.toggleVisibility(article.likes != 0)
    //todo colors
    itemView.likes.text = (if (article.likes > 0) "+" else "-") + article.likes
    itemView.setOnClickListener({
      eventBus.post(DownloadArticleEvent(article))
    })
  }
}

class DownloadArticleEvent(val article: ArticlePreview) {}