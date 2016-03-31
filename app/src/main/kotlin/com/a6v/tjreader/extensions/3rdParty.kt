package com.a6v.tjreader.extensions

import com.google.gson.JsonObject

fun JsonObject.getAsNullableJsonObject(key: String): JsonObject? {
  val json = get(key)
  if (json != null && json.isJsonObject) {//TODO throw warning if not object?
    return json.asJsonObject
  }
  return null
}