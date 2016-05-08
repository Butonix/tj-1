package com.a6v.tjreader.extensions

import rx.Single
import rx.SingleSubscriber
import rx.Subscription
import rx.exceptions.OnErrorNotImplementedException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Future

fun <T> single(body: (s: SingleSubscriber<in T>) -> Unit): Single<T> = Single.create(body)
fun <T> singleOf(value: T): Single<T> = Single.just(value)
fun <T> Future<T>.toSingle(): Single<T> = Single.from(this)
fun <T> Callable<T>.toSingle(): Single<T> = Single.fromCallable { this.call() }
fun <T> Throwable.toSingle(): Single<T> = Single.error(this)

/**
 * Subscribe with a subscriber that is configured inside body
 */
inline fun <T> Single<T>.subscribeWith(body: FunctionSingleSubscriberModifier<T>.() -> Unit): Subscription {
  val modifier = FunctionSingleSubscriberModifier(singleSubscriber<T>())
  modifier.body()
  return subscribe(modifier.subscriber)
}

class FunctionSingleSubscriber<T>() : SingleSubscriber<T>() {
  private val onSuccessFunctions = ArrayList<(value: T) -> Unit>()
  private val onErrorFunctions = ArrayList<(e: Throwable) -> Unit>()

  override fun onSuccess(t: T) = onSuccessFunctions.forEach { it(t) }

  override fun onError(e: Throwable?) = (e ?: RuntimeException("exception is unknown")).let { ex ->
    if (onErrorFunctions.isEmpty()) {
      throw OnErrorNotImplementedException(ex)
    } else {
      onErrorFunctions.forEach { it(ex) }
    }
  }

  fun onSuccess(onSuccessFunction: (t: T) -> Unit): FunctionSingleSubscriber<T> = copy { onSuccessFunctions.add(onSuccessFunction) }
  fun onError(onErrorFunction: (e: Throwable) -> Unit): FunctionSingleSubscriber<T> = copy { onErrorFunctions.add(onErrorFunction) }

  private fun copy(block: FunctionSingleSubscriber<T>.() -> Unit): FunctionSingleSubscriber<T> {
    val newSubscriber = FunctionSingleSubscriber<T>()
    newSubscriber.onSuccessFunctions.addAll(onSuccessFunctions)
    newSubscriber.onErrorFunctions.addAll(onErrorFunctions)

    newSubscriber.block()

    return newSubscriber
  }
}

class FunctionSingleSubscriberModifier<T>(init: FunctionSingleSubscriber<T> = singleSubscriber()) {
  var subscriber: FunctionSingleSubscriber<T> = init
    private set

  fun onSuccess(onSuccessFunction: (t: T) -> Unit): Unit { subscriber = subscriber.onSuccess(onSuccessFunction) }
  fun onError(onErrorFunction: (r: Throwable) -> Unit): Unit {subscriber = subscriber.onError(onErrorFunction) }
}

fun <T> singleSubscriber(): FunctionSingleSubscriber<T> = FunctionSingleSubscriber()