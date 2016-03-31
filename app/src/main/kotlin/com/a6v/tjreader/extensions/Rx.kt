package com.a6v.tjreader.extensions

/**
 * Created by alexey on 25.02.16.
 */
@Suppress("CAST_NEVER_SUCCEEDS")
fun <T> rx.Observable<T?>.filterNonNull(): rx.Observable<T> {
  return this.filter { it != null } as rx.Observable<T>
}