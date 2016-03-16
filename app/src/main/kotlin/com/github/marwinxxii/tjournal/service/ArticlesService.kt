package com.github.marwinxxii.tjournal.service

import android.net.Uri
import com.github.marwinxxii.tjournal.CompositeDiskStorage
import com.github.marwinxxii.tjournal.ImageLoaderImpl
import com.github.marwinxxii.tjournal.entities.Article
import com.github.marwinxxii.tjournal.entities.ArticlePreview
import com.github.marwinxxii.tjournal.network.TJournalAPI
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import rx.Observable

/**
 * Created by alexey on 21.02.16.
 */

class ArticlesService(
  private val api: TJournalAPI,
  private val dao: ArticlesDAO,
  private val downloader: ArticleDownloadService,
  private val imageDiskStorage: CompositeDiskStorage,
  private val imageLoader: ImageLoaderImpl) {

  fun getArticles(page: Int): Observable<List<ArticlePreview>> {
    //TODO use deserializer?
    return api.getNews(page * 30)//TODO switch to computation
      .flatMap { previews ->
        dao.getSavedIds(previews.map { it.id })
          .map { savedIds ->
            previews.filter { preview -> !savedIds.contains(preview.id) }
          }
      }
      .map {
        it.map {
          val intro = Jsoup.parse(it.intro).text()
          it.copy(intro = intro)
        }
      }
  }

  fun getArticle(preview: ArticlePreview): Observable<Article> {
    return dao.getArticle(preview.id)
      .switchIfEmpty(
        dao.enqueue(preview)
          .flatMap { saved ->
            downloadArticle(preview.url)
              //TODO change text saving
              .doOnNext { text -> dao.saveText(saved._id, text) }
              //TODO handle error
              .map { Article(saved, it) }
              .zipWith(
                Observable.fromCallable {
                  if (saved.cover != null) {
                    imageDiskStorage.copyToPermanent(saved.cover.thumbnailUrl)
                  }
                }, //TODO handle error?

                { article, coverCopied -> article }
              )
          }
      )
  }

  private fun downloadArticle(url: String): Observable<String> {
    return downloader.downloadArticle(url).map {
      val parser = ArticleHtmlParser(it)
      parser.replaceVideosWithThumbnails()
      for (image in parser.findImageUrls()) {
        imageLoader.downloadImage(image, true)//TODO check image load result?
      }
      parser.getHtml()
    }
  }

  fun getArticle(id: Int): Article? {
    return dao.getArticleSync(id)
  }

  fun observeArticleCount(): Observable<ArticleCount> {
    return dao.observeArticleChanges().startWith("")
      .flatMap {
        Observable.zip(
          dao.countUnreadArticles(),
          dao.countArticlesByStatus(READY),
          { total, ready -> ArticleCount(total, ready) }
        )
      }
  }

  fun getSavedPreviews(): Observable<List<ArticlePreview>> {
    return dao.observeArticleChanges().startWith("")
      .flatMap { dao.getPreviews("status=" + READY) }
  }
}

data class ArticleCount(val total: Int, val loaded: Int)

const val WAITING = 0
const val LOADING: Int = 1
const val ERROR: Int = 2
const val READY: Int = 3
const val READ: Int = 4

class ArticleHtmlParser(document: Document) {
  private val article = getArticle(document)

  fun getArticle(document: Document): Element {
    return document.getElementsByTag("article").first()
  }

  fun findImageUrls(): List<String> {
    return article.getElementsByTag("img")
      .map { it.attr("src") }
      .filter { it != null && !it.isEmpty() }
  }

  fun replaceVideosWithThumbnails(): ArticleHtmlParser {
    for (iframe in article.getElementsByTag("iframe")) {
      val src: String? = iframe.attr("src")
      if (src != null) {
        iframe.replaceWith(createIframeReplacement(src))
      }
    }
    return this
  }

  fun createIframeReplacement(src: String): Element {
    val videoId = Uri.parse(src).encodedPath.replaceFirst("/embed/", "")
    val link = Element(Tag.valueOf("a"), article.baseUri())
    link.attr("href", "https://www.youtube.com/watch?v=$videoId")//TODO styles

    val img = Element(Tag.valueOf("img"), article.baseUri())
    img.attr("src", "https://img.youtube.com/vi/$videoId/0.jpg")//TODO choose image size

    link.appendChild(img)
    return link
  }

  fun getHtml(): String {
    return article.outerHtml()
  }
}