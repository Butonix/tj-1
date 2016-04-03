package com.a6v.tjreader.entities

import java.util.*

data class ArticlePreview(
  val id: Int,
  val title: String,
  val url: String,
  val intro: String,
  val date: Date,
  val commentsCount: Int,
  val likes: Int,
  val cover: CoverPhoto?,
  val externalLink: ArticleExternalSource?,
  val hasFullText: Boolean,
  //non persisted field
  val introHtml: String? = null
)

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