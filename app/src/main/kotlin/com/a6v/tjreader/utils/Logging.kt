package com.a6v.tjreader.utils

import android.util.Log
import com.a6v.tjreader.BuildConfig

/**
 * Created by alexey on 20.02.16.
 */
fun logError(message: String, exception: Throwable) {
  if (BuildConfig.LOG_ENABLED) {
    Log.e("TJogger", message, exception)
  }
}

fun logError(exception: Throwable) {
  logError("logError", exception)
}

fun logd(message: String) {
  if (BuildConfig.LOG_ENABLED) {
    Log.d("TJogger", message)
  }
}