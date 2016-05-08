package com.a6v.tjreader.activities

import java.util.*

class ActivityRetainedState : HashMap<String, Any>() {
  inline fun <T> getOrCreate(key: String, factory: () -> T): T {
    val t = get(key) as T
    if (t != null) {
      return t
    }
    return factory()
  }

  fun putIfNotPresent(key: String, value: Any) {
    if (!containsKey(key)) {
      put(key, value)
    }
  }
}
