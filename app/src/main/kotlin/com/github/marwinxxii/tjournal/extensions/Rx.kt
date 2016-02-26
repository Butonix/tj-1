package com.github.marwinxxii.tjournal.extensions

import rx.Observable

/**
 * Created by alexey on 25.02.16.
 */
@Suppress("CAST_NEVER_SUCCEEDS")
fun <T> Observable<T?>.filterNonNull(): Observable<T> {
  return this.filter { it != null } as Observable<T>
}