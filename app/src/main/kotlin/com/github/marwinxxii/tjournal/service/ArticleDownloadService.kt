package com.github.marwinxxii.tjournal.service

import com.github.marwinxxii.tjournal.extensions.filterNonNull
import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import org.jsoup.Jsoup
import rx.Observable
import rx.Subscriber
import java.io.IOException

/**
 * Created by alexey on 21.02.16.
 */
class ArticleDownloadService(val httpClient: OkHttpClient) {
  fun downloadArticleText(url: String): Observable<String> {
    return observeNetworkRequest(url)
      //TODO observe on computation?
      .filterNonNull()
      .map { response ->
        val document = Jsoup.parse(response.body().byteStream(), "UTF-8", url)
        document.getElementsByTag("article").first().outerHtml()
      }
  }

  private fun observeNetworkRequest(url: String): Observable<Response?> {
    return Observable.create { subscriber ->
      httpClient
        .newCall(Request.Builder().get().url(url).build())
        .enqueue(SubscriberCallback(subscriber))
    }
  }
}

class SubscriberCallback(val subscriber: Subscriber<in Response?>) : Callback {
  override fun onResponse(response: Response?) {
    if (!subscriber.isUnsubscribed) {
      subscriber.onNext(response)
      //TODO handle HTTP erorrs
    }
    subscriber.onCompleted()
  }

  override fun onFailure(request: Request, exception: IOException) {
    subscriber.onError(exception)
  }
}