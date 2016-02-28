package com.github.marwinxxii.tjournal.extensions

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.view.View
import android.widget.TextView
import com.github.marwinxxii.tjournal.App
import com.github.marwinxxii.tjournal.AppComponent

/**
 * Created by alexey on 20.02.16.
 */

fun <T : Context> T.getApp(): App = this.applicationContext as App

fun <T : Context> T.getAppComponent(): AppComponent = getApp().component

fun <T> Activity.startActivityWithClass(cl: Class<T>) {
  startActivity(Intent(this, cl))
}

fun <T : View> T.toggleVisibility(visible: Boolean) {
  this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun <T : TextView> T.toggleVisibilityAndText(visible: Boolean, text: CharSequence) {
  if (visible) {
    this.text = text
    this.visibility = View.VISIBLE
  } else {
    this.visibility = View.GONE
  }
}

fun <T : TextView> T.toggleVisibilityAndText(visible: Boolean, value: Any) {
  return toggleVisibilityAndText(visible, value.toString())
}

fun SQLiteDatabase.insert(tableName: String, vararg values: Pair<String, Any?>): Long {
  return insert(tableName, null, values._toContentValues())
}

fun Array<out Pair<String, Any?>>._toContentValues(): ContentValues {
  val values = ContentValues()
  for ((key, value) in this) {
    when (value) {
      is Boolean -> values.put(key, value)
      is Byte -> values.put(key, value)
      is ByteArray -> values.put(key, value)
      is Double -> values.put(key, value)
      is Float -> values.put(key, value)
      is Int -> values.put(key, value)
      is Long -> values.put(key, value)
      is Short -> values.put(key, value)
      is String -> values.put(key, value)
      is String? -> values.putNull(key)
      else -> throw IllegalArgumentException("Non-supported value type: ${value?.javaClass?.name}")
    }
  }
  return values
}

inline fun <T> Cursor.getValueFromCursor(columnName: String, getter: (Cursor, Int) -> T): T {
  return getter(this, this.getColumnIndex(columnName))
}

fun Cursor.getLong(columnName: String): Long = getValueFromCursor(columnName, Cursor::getLong)

fun Cursor.getInt(columnName: String): Int = getValueFromCursor(columnName, Cursor::getInt)

fun Cursor.getString(columnName: String): String? = getValueFromCursor(columnName, Cursor::getString)
