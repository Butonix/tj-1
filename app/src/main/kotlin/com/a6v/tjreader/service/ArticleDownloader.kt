package com.a6v.tjreader.service

import com.a6v.tjreader.CompositeDiskStorage
import com.a6v.tjreader.ImageLoaderImpl
import com.a6v.tjreader.db.ImageState
import com.a6v.tjreader.db.ImagesDao
import com.a6v.tjreader.entities.Article
import com.a6v.tjreader.entities.ArticlePreview
import com.a6v.tjreader.entities.ArticleStatus
import com.a6v.tjreader.network.HtmlDownloader
import rx.Observable
import rx.Single

class ArticleDownloader(
  private val articlesDAO: ArticlesDAO,
  private val imagesDao: ImagesDao,
  private val htmlDownloader: HtmlDownloader,
  private val imageLoader: ImageLoaderImpl,
  private val imageDiskStorage: CompositeDiskStorage
) {
  fun downloadArticle(preview: ArticlePreview): Single<Article> {
    return articlesDAO.savePreview(preview, ArticleStatus.WAITING)
      .flatMap { saved ->
        val thumbnail = saved.cover?.thumbnailUrl
        val thumbnailSaving: Single<Long>
        if (thumbnail != null) {
          thumbnailSaving = saveThumbnail(saved, thumbnail)
        } else {
          thumbnailSaving = Single.just(0)
        }
        Single.zip(
          thumbnailSaving,

          htmlDownloader
            .download(saved.url)
            .retry(2)
            .map {
              val parser = ArticleHtmlParser(it)
              parser.replaceVideosWithThumbnails()
            }
            .flatMap { parser ->
              Single.zip(
                Single.fromCallable {
                  val text = parser.getHtml()
                  articlesDAO.saveText(saved.id, text)
                  Article(saved, text)
                },

                Observable.from(parser.findImageUrls())
                  .filter { it != thumbnail }
                  .distinct()
                  .flatMap {
                    saveImageAndLoad(saved.id, it).toObservable()
                  }.lastOrDefault(0).toSingle(),

                { article, imagesLoadedTrue -> article}
              )
            },

          { thumbnailId, article -> article }
        )
      }
  }

  fun downloadPendingArticles() {
    articlesDAO.queryArticles(ArticleStatus.ERROR)
    //.check text
    //queryarticles
    //flatmap images
    //download image
  }

  private fun saveThumbnail(saved: ArticlePreview, thumbnail: String): Single<Long> {
    if (imageDiskStorage.tempExists(thumbnail)) {
      return Single.defer {
        imageDiskStorage.copyToPermanent(thumbnail)//TODO file can be deleted
        imagesDao.saveImage(saved.id, thumbnail, ImageState.LOADED)
      }
    } else {
      return saveImageAndLoad(saved.id, thumbnail)
    }
  }

  private fun saveImageAndLoad(articleId: Int, url: String): Single<Long> {
    return imagesDao.saveImage(articleId, url, ImageState.NOT_LOADED)
      .flatMap { _id ->
        Single.fromCallable {
          imageLoader.downloadImage(url, true)
          _id
        }
          .retry(2)
          .flatMap { _id ->
            imagesDao.updateImage(_id, ImageState.LOADED)
              .toSingleDefault(_id)
          }
      }
  }
}