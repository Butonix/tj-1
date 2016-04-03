package com.a6v.tjreader.service

import com.a6v.tjreader.entities.Article
import com.a6v.tjreader.entities.ArticlePreview
import com.a6v.tjreader.entities.ArticleStatus
import com.a6v.tjreader.network.HtmlDownloader
import rx.Completable
import rx.Observable
import rx.Single

class ArticleDownloader(
  private val articlesDAO: ArticlesDAO,
  private val htmlDownloader: HtmlDownloader,
  private val imageDownloader: ImageDownloader
) {
  fun downloadArticle(preview: ArticlePreview): Single<Article> {
    return articlesDAO.savePreview(preview, ArticleStatus.WAITING)
      .flatMap { downloadAndUpdateArticle(it) }
  }

  fun downloadAndUpdateArticle(saved: ArticlePreview): Single<Article> {
    val thumbnail = saved.cover?.thumbnailUrl
    val thumbnailSaving: Completable
    if (thumbnail != null) {
      thumbnailSaving = imageDownloader.saveThumbnail(saved, thumbnail)
    } else {
      thumbnailSaving = Completable.complete()
    }
    return Single.zip(
      thumbnailSaving.toSingleDefault(0),

      htmlDownloader
        .download(saved.url)
        .retry(2)
        .map {
          val parser = ArticleHtmlParser(it)
          parser.replaceVideosWithThumbnails()
        }
        .flatMap { parser ->
          Single.zip(
            articlesDAO.updateArticle(saved, parser.getHtml()),

            Observable.from(parser.findImageUrls())
              .filter { it != thumbnail }
              .distinct()
              .flatMap {
                imageDownloader.saveImageAndLoad(saved.id, it).toObservable<Long>()
              }.lastOrDefault(0).toSingle(),

            { article, imagesLoadedTrue -> article }
          )
        },

      { thumbnailSaved, article -> article }
    )
      .flatMap {
        articlesDAO.updateArticle(it.preview.id, ArticleStatus.READY)
          .toSingleDefault(it)
      }
  }

  fun downloadPendingArticles(): Completable {
    return articlesDAO.queryArticles(ArticleStatus.WAITING)
      .flatMap { downloadAndUpdateArticle(it).toObservable() }
      .toCompletable()
  }

  /*fun downloadPendingImages(): Single<Boolean> {
    return imagesDao.queryImages(ImageState.NOT_LOADED)
      .flatMap {
        downloadAndUpdateImage(0, it).toObservable<Boolean>()
      }
      .lastOrDefault(false)
      .toSingle()
  }*/
}