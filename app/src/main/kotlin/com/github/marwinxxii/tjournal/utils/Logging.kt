package com.github.marwinxxii.tjournal.utils

import android.util.Log

/**
 * Created by alexey on 20.02.16.
 */
fun logError(message: String, exception: Throwable) {
  Log.e("TJogger", message, exception)
}

fun logError(exception: Throwable) {
  logError("logError", exception)
}

fun logd(message: String) {
  Log.d("TJogger", message)
}