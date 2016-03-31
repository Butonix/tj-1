package com.a6v.tjreader.service

import com.a6v.tjreader.extensions.filterNonNull
import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import rx.Observable
import rx.Subscriber
import java.io.IOException

/**
 * Created by alexey on 21.02.16.
 */
class ArticleDownloadService(private val httpClient: OkHttpClient) {
  fun downloadArticle(url: String): Observable<Document> {
    return observeNetworkRequest(url)
      //TODO observe on computation?
      .map { response ->
        Jsoup.parse(response.body().byteStream(), "UTF-8", url)
      }
      .filterNonNull()
  }

  private fun observeNetworkRequest(url: String): Observable<Response> {
    return Observable.create { subscriber ->
      httpClient
        .newCall(Request.Builder().get().url(url).build())
        .enqueue(SubscriberCallback(subscriber))
    }
  }
}

class SubscriberCallback(val subscriber: Subscriber<in Response>) : Callback {
  override fun onResponse(response: Response) {
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