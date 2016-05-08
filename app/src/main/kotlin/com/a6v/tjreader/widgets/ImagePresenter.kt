package com.a6v.tjreader.widgets

import android.widget.ImageView
import com.a6v.tjreader.ImageLoaderImpl
import javax.inject.Inject

interface ImagePresenter {
  fun displayImage(url: String, view: ImageView)
}

class TempImagePresenter : ImagePresenter {
  private val loader: ImageLoaderImpl

  @Inject
  constructor(loader: ImageLoaderImpl) {
    this.loader = loader
  }

  override fun displayImage(url: String, view: ImageView) {
    loader.displayImage(url, view)
  }
}

class PermanentImagePresenter : ImagePresenter {
  private val loader: ImageLoaderImpl

  @Inject
  constructor(loader: ImageLoaderImpl) {
    this.loader = loader
  }

  override fun displayImage(url: String, view: ImageView) {
    loader.displayImage(url, view, true)
  }
}
