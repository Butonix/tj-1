package com.a6v.tjreader.network

import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import rx.Single
import rx.SingleSubscriber
import java.io.IOException

/**
 * Created by alexey on 21.02.16.
 */
class HtmlDownloader(private val httpClient: OkHttpClient) {
  fun download(url: String): Single<Document> {
    return performNetworkRequest(url)
      //TODO observe on computation?
      .map { response ->
        Jsoup.parse(response.body().byteStream(), "UTF-8", url)
      }
  }

  private fun performNetworkRequest(url: String): Single<Response> {
    return Single.create { subscriber ->
      httpClient
        .newCall(Request.Builder().get().url(url).build())
        .enqueue(SingleSubscriberCallback(subscriber))
    }
  }
}

class SingleSubscriberCallback(val subscriber: SingleSubscriber<in Response>) : Callback {
  override fun onResponse(response: Response) {
    subscriber.onSuccess(response)
    //TODO handle HTTP erorrs
  }

  override fun onFailure(request: Request, exception: IOException) {
    subscriber.onError(exception)
  }
}