package com.a6v.tjreader.fragments

import android.content.Context
import com.trello.rxlifecycle.components.support.RxFragment

abstract class BaseFragment : RxFragment() {
  override fun onAttach(context: Context) {
    super.onAttach(context)
    injectSelf()
  }

  abstract fun injectSelf()
}