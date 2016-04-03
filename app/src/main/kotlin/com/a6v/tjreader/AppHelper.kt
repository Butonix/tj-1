package com.a6v.tjreader

import android.content.Context
import com.a6v.tjreader.utils.logError
import org.jetbrains.anko.defaultSharedPreferences
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File

object AppHelper {
  fun observeVersionChanged(context: Context): Observable<Boolean> {
    return Observable.fromCallable {
      val previousVersion = context.defaultSharedPreferences.getInt("appVersion", 0)
      if (BuildConfig.VERSION_CODE != previousVersion) {
        context.deleteDatabase("tjournal.db")

        context.cacheDir.listFiles().forEach { it.delete() }
        File(context.filesDir, "permanent").deleteRecursively()
        context.defaultSharedPreferences.edit().putInt("appVersion", BuildConfig.VERSION_CODE).commit()
        return@fromCallable true
      }
      false
    }
      .doOnError { logError(it) }
      .onErrorResumeNext(Observable.empty())
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
  }
}