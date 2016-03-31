package com.a6v.tjreader.entities

import java.util.*

data class ArticlePreview(
  //internal fields
  val _id: Long,
  val status: ArticleStatus,
  //fields from api
  val id: Int,
  val title: String,
  val url: String,
  val intro: String,
  val date: Date,
  val commentsCount: Int,
  val likes: Int,
  val cover: CoverPhoto?,
  val externalLink: ArticleExternalSource?
) {
  constructor(id: Int,
    title: String,
    url: String,
    intro: String,
    date: Date,
    commentsCount: Int,
    likes: Int,
    cover: CoverPhoto?,
    source: ArticleExternalSource?) :
  this(0, ArticleStatus.WAITING, id, title, url, intro, date, commentsCount, likes, cover, source) {
  }
}

data class Article(val preview: ArticlePreview, val text: String)

data class CoverPhoto(val thumbnailUrl: String, val url: String)

enum class ArticleStatus {
  WAITING,
  LOADING,
  ERROR,
  READY,
  READ
}

data class ArticleExternalSource(val domain: String, val url: String)