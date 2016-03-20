package com.github.marwinxxii.tjournal.activities

import android.os.Bundle
import com.trello.rxlifecycle.components.support.RxAppCompatActivity

/**
 * Created by alexey on 08.03.16.
 */
abstract class BaseActivity : RxAppCompatActivity() {
  abstract fun initComponent()

  override fun onCreate(savedInstanceState: Bundle?) {
    //component must be initialized before fragments were restored (in super)
    initComponent()
    super.onCreate(savedInstanceState)
  }
}