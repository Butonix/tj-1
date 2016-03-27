package com.github.marwinxxii.tjournal.service

import android.net.Uri
import com.github.marwinxxii.tjournal.CompositeDiskStorage
import com.github.marwinxxii.tjournal.ImageLoaderImpl
import com.github.marwinxxii.tjournal.entities.Article
import com.github.marwinxxii.tjournal.entities.ArticlePreview
import com.github.marwinxxii.tjournal.entities.ArticleStatus
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

  fun downloadArticle(preview: ArticlePreview): Observable<Article> {
    return dao.savePreview(preview)
      .flatMap { saved ->
        downloader
          .downloadArticle(saved.url)
          .retry(2)
          .map {
            val parser = ArticleHtmlParser(it)
            parser.replaceVideosWithThumbnails()
          }
          .flatMap { parser ->
            Observable.zip(
              Observable.fromCallable {
                val text = parser.getHtml()
                dao.saveText(saved.id, text)
                if (saved.cover != null) {
                  imageDiskStorage.copyToPermanent(saved.cover.thumbnailUrl)
                }
                Article(saved, text)
              },
              Observable.from(parser.findImageUrls())
                .flatMap {
                  Observable.fromCallable {
                    imageLoader.downloadImage(it, true)
                    true
                  }.retry(2)
                }.lastOrDefault(true),
              { article, imagesLoadedTrue -> article }
            )
          }
      }
  }

  fun getArticle(id: Int): Observable<Article> {
    return dao.getArticle(id)
  }

  fun observeArticleCount(): Observable<ArticleCount> {
    return dao.observeArticleChanges().startWith("")
      .flatMap {
        Observable.zip(
          dao.countUnreadArticles(),
          dao.countArticlesByStatus(ArticleStatus.READY),
          { total, ready -> ArticleCount(total, ready) }
        )
      }
  }

  fun getSavedPreviews(): Observable<List<ArticlePreview>> {
    return dao.observeArticleChanges().startWith("")
      .flatMap { dao.getPreviews("status=" + dao.statusToInt(ArticleStatus.READY)) }
  }
}

data class ArticleCount(val total: Int, val loaded: Int)

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
    val div = Element(Tag.valueOf("div"), article.baseUri())
    div.attr("data-tjr-thumbnail", true)//in case of future need to find these thumbnails
    val link = Element(Tag.valueOf("a"), article.baseUri())
    link.attr("href", "https://www.youtube.com/watch?v=$videoId")//TODO styles

    val img = Element(Tag.valueOf("img"), article.baseUri())
    img.attr("src", "https://img.youtube.com/vi/$videoId/0.jpg")//TODO choose image size

    link.appendChild(img)
    div.appendChild(link)
    div.appendElement("span").text("(кликните, чтобы открыть YouTube видео)")
    return div
  }

  fun getHtml(): String {
    return article.outerHtml()
  }
}