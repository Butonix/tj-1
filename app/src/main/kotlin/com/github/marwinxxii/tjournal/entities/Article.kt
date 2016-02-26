package com.github.marwinxxii.tjournal.entities

import com.github.marwinxxii.tjournal.service.WAITING
import java.util.*

data class ArticlePreview(
  var _id: Long,
  val status: Int,
  val id: Int,
  val title: String,
  val url: String,
  val intro: String,
  val date: Date,
  val commentsCount: Int,
  val likes: Int,
  val cover: CoverPhoto?
) {
  constructor(id: Int,
    title: String,
    url: String,
    intro: String,
    date: Date,
    commentsCount: Int,
    likes: Int,
    cover: CoverPhoto?) :
  this(0, WAITING, id, title, url, intro, date, commentsCount, likes, cover) {
  }
}

data class Article(val preview: ArticlePreview, val text: String)

data class CoverPhoto(val thumbnailUrl: String, val url: String)