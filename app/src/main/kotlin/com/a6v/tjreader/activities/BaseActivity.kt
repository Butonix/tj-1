package com.a6v.tjreader.activities

import android.os.Bundle
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import javax.inject.Inject

/**
 * Created by alexey on 08.03.16.
 */
abstract class BaseActivity : RxAppCompatActivity() {
  @Inject lateinit var retainedState: ActivityRetainedState

  abstract fun initComponent()

  override fun onCreate(savedInstanceState: Bundle?) {
    //component must be initialized before fragments were restored (in super)
    initComponent()
    super.onCreate(savedInstanceState)
  }

  override fun onRetainCustomNonConfigurationInstance(): Any {
    return retainedState
  }
}