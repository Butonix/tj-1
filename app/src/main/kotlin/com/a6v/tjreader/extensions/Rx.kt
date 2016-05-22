package com.a6v.tjreader.extensions

import rx.Completable
import rx.Single
import rx.SingleSubscriber
import rx.Subscription

@Suppress("CAST_NEVER_SUCCEEDS")
fun <T> rx.Observable<T?>.filterNonNull(): rx.Observable<T> {
  return this.filter { it != null } as rx.Observable<T>
}

fun <T> Single<T>.flatMapCompletable(f: (T) -> Completable): Completable {
  return Completable.fromSingle(this.flatMap { f(it).toSingleDefault(it) } )
}

fun <T> Single<T>.subscribe_(onSuccess: (T) -> Unit, onError: (Throwable) -> Unit)
  : Subscription
{
  return subscribe(object: SingleSubscriber<T>() {
    override fun onSuccess(value: T) {
      onSuccess(value)
    }

    override fun onError(error: Throwable) {
      onError(error)
    }
  })
}