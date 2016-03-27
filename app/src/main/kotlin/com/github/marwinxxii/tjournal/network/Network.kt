package com.github.marwinxxii.tjournal.network

import com.github.marwinxxii.tjournal.entities.ArticlePreview
import com.github.marwinxxii.tjournal.entities.ArticleExternalSource
import com.github.marwinxxii.tjournal.entities.CoverPhoto
import com.github.marwinxxii.tjournal.extensions.getAsNullableJsonObject
import com.google.gson.*
import com.squareup.okhttp.OkHttpClient
import dagger.Module
import dagger.Provides
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import retrofit.http.GET
import retrofit.http.Query
import rx.Observable
import java.lang.reflect.Type
import java.util.*
import javax.inject.Singleton

/**
 * Created by alexey on 20.02.16.
 */

interface TJournalAPI {
  @GET("/club?type=0&sortMode=mainpage")
  fun getNews(@Query("offset") offset: Int): Observable<List<ArticlePreview>>;
}

@Module
class NetworkModule {
  @Provides
  @Singleton
  fun provideAPI(client: OkHttpClient, gson: Gson): TJournalAPI {
    return RestAdapter.Builder()
      .setClient(OkClient(client))
      .setEndpoint("https://api.tjournal.ru/2.2/")
      .setLogLevel(RestAdapter.LogLevel.FULL)
      .setConverter(GsonConverter(gson))
      .build()
      .create(TJournalAPI::class.java)
  }

  @Provides
  @Singleton
  fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient()
  }

  @Provides
  fun provideGson(): Gson {
    return GsonBuilder()
      .registerTypeAdapter(ArticlePreview::class.java, ArticlePreviewDeserializer())
      .create()
  }
}

class ArticlePreviewDeserializer : JsonDeserializer<ArticlePreview> {
  override fun deserialize(element: JsonElement, cl: Type, context: JsonDeserializationContext): ArticlePreview {
    //TODO validation
    val json = element.asJsonObject
    val id = json.getAsJsonPrimitive("id").asInt
    val title = json.getAsJsonPrimitive("title").asString
    val url = json.getAsJsonPrimitive("url").asString
    val intro = json.getAsJsonPrimitive("intro").asString
    val date = Date(json.getAsJsonPrimitive("date").asLong)
    val commentsCount = json.getAsJsonPrimitive("commentsCount").asInt
    val likes = json.getAsJsonObject("likes").getAsJsonPrimitive("summ").asInt

    var cover: CoverPhoto? = null
    val coverJson = json.getAsNullableJsonObject("cover")
    if (coverJson != null) {
      val thumbnail = coverJson.get("thumbnailUrl")?.asString
      val full = coverJson.get("url")?.asString
      if (thumbnail != null && full != null) {
        cover = CoverPhoto(thumbnail, full)
      }
    }
    var external: ArticleExternalSource? = null
    val externalJson = json.getAsNullableJsonObject("externalLink")
    if (externalJson != null) {
      val externalDomain = externalJson.get("domain")?.asString
      val externalUrl = externalJson.get("url")?.asString
      if (externalDomain != null && externalUrl != null) {
        external = ArticleExternalSource(externalDomain, externalUrl)
      }
    }
    //TODO fix
    return ArticlePreview(id, title, url, intro, date, commentsCount, likes, cover, external)
  }
}