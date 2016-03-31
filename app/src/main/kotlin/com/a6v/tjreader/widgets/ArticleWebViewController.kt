package com.a6v.tjreader.widgets

import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.a6v.tjreader.CompositeDiskStorage
import com.a6v.tjreader.EventBus
import com.a6v.tjreader.entities.Article
import com.a6v.tjreader.entities.ArticleExternalSource
import com.a6v.tjreader.extensions.isActivityResolved
import com.a6v.tjreader.service.ArticlesService
import rx.Observable
import java.io.FileInputStream
import java.io.InputStream
import javax.inject.Inject

class ArticleWebViewController {
  val view: WebView

  @Inject
  constructor(view: WebView, imageCache: CompositeDiskStorage, service: ArticlesService,
    eventBus: EventBus)
  {
    this.view = view
    this.view.setWebViewClient(ImageInterceptor(imageCache, service, eventBus))
  }

  fun loadArticle(id: Int) {
    view.loadUrl("tjournal:$id")
  }
}

class ImageInterceptor(val imageCache: CompositeDiskStorage, val service: ArticlesService,
  val eventBus: EventBus): WebViewClient()
{
  override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    val context = view.context
    if (context.isActivityResolved(intent)) {
      context.startActivity(intent)
      return true
    }
    return false
  }

  override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
    val url = request.url
    if (request.method == "GET") {
      val scheme = url.scheme
      when (scheme) {
        "http", "https" -> return tryLoadImage(url.toString())
        "tjournal" -> return loadArticle(url.toString())
      }
    }
    return null
  }

  override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
    if (url.startsWith("tjournal:")) {
      return loadArticle(url)
    }
    if (url.startsWith("http://") || url.startsWith("https://")) {
      return tryLoadImage(url)
    }
    return null
  }

  private fun loadArticle(url: String): WebResourceResponse {
    return WebResourceResponse("text/html", "utf-8", ArticleInputStream(
      Observable.concat(
        Observable.just(articleHead),
        service.getArticle(url.removePrefix("tjournal:").toInt())
          .flatMap {
            eventBus.post(ArticleLoadedEvent(it))
            val title = "<h2>" + it.preview.title + "</h2>\n"
            if (it.preview.externalLink != null) {
              Observable.just(
                title, createExternalSourceElement(it.preview.externalLink), it.text
              )
            } else {
              Observable.just(title, it.text)
            }
          }
          .onErrorResumeNext(Observable.just(onErrorMessage))
          .defaultIfEmpty(onErrorMessage),
        Observable.just("</body></html>")
      ).toBlocking().toIterable()
    ))
  }

  private fun tryLoadImage(url: String): WebResourceResponse? {
    val file = imageCache.getPermanent(url)
    if (file != null && !file.isDirectory) {
      return WebResourceResponse("image/jpeg", null, FileInputStream(file))
    }
    return null
  }

  private fun createExternalSourceElement(source: ArticleExternalSource): String {
    return "<p>Source: <a href=\"${source.url}\">${source.domain}</a></p>"
  }
}

const val onErrorMessage = "Article could not be loaded, try again"
const val articleHead = "<!doctype html>" +
  "<html>" +
  "<head>" +
  "<meta charset=\"utf-8\">" +
  "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
  "<style>article img { max-width: 100%;}</style>" +
  "</head>" +
  "<body>"

class ArticleInputStream(stringsProvider: Iterable<String>) : InputStream() {
  private val strings = stringsProvider.iterator()
  private var position = 0
  private var currentByteRange = 0
  private var currentByteArray = ByteArray(0)

  override fun read(): Int {
    if (position >= currentByteRange) {
      if (!strings.hasNext()) {
        return -1
      }
      val newArray = strings.next().toByteArray(charset("utf-8"))
      currentByteRange = newArray.size
      currentByteArray = newArray
      position = 0
    }
    return currentByteArray[position++].toInt()
  }
}

data class ArticleLoadedEvent(val article: Article)
