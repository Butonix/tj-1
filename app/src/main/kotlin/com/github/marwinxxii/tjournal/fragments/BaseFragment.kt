package com.github.marwinxxii.tjournal.fragments

import android.content.Context
import com.trello.rxlifecycle.components.support.RxFragment

/**
 * Created by alexey on 08.03.16.
 */
abstract class BaseFragment : RxFragment() {
  override fun onAttach(context: Context?) {
    super.onAttach(context)
    injectSelf()
  }

  abstract fun injectSelf()
}