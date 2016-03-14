package com.github.marwinxxii.tjournal.extensions

fun <T> generator(body: Generator<T>.() -> Generator<T>): Iterable<T> {
  val generator = Generator<T>()
  generator.body()
  return generator
}

class Generator<T> : Iterable<T> {
  private val functions = mutableListOf<Function0<T>>()

  override fun iterator(): Iterator<T> {
    return object: Iterator<T> {
      private var index = 0
      override fun hasNext(): Boolean {
        return index < functions.size
      }

      override fun next(): T {
        return functions[index++]()
      }
    }
  }

  fun yieldReturn(func: () -> T): Generator<T> {
    functions.add(func)
    return this
  }

  fun yieldReturn(value: T): Generator<T> {
    return yieldReturn { value }
  }

  fun yieldBreak(): Generator<T> {
    return this
  }
}