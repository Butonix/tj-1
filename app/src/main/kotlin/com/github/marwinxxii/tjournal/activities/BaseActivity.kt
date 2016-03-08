package com.github.marwinxxii.tjournal.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by alexey on 08.03.16.
 */
abstract class BaseActivity : AppCompatActivity() {
  abstract fun initComponent()

  override fun onCreate(savedInstanceState: Bundle?) {
    //component must be initialized before fragments were restored (in super)
    initComponent()
    super.onCreate(savedInstanceState)
  }
}