package com.a6v.tjreader.utils

import android.support.v7.widget.RecyclerView
import rx.Observable
import rx.Observer
import rx.subjects.PublishSubject
import java.util.*

interface DataSource {
  fun observeData(): Observable<DataSourceEvent>
}

sealed class DataSourceEvent {
  class Load(private val data: Any, val count: Int) : DataSourceEvent() {
    fun <T> getData(): T = data as T
  }

  class Change(private val data: Any, val count: Int) : DataSourceEvent() {
    fun <T> getData(): T = data as T
  }

  class Insert(private val item: Any, val position: Int) : DataSourceEvent() {
    fun <T> getItem(): T = item as T
  }

  class Remove(val position: Int) : DataSourceEvent()
}

class ObservableList<T : Any> : DataSource {
  private val items = mutableListOf<T>()
  private val subject = PublishSubject.create<DataSourceEvent>()

  fun set(items: List<T>) {
    this.items.clear()
    addAll(items)
  }

  fun clear() = set(Collections.emptyList())

  fun size(): Int = items.size

  fun get(position: Int): T = items[position]

  fun addAll(items: List<T>) {
    this.items.addAll(items)
    subject.onNext(DataSourceEvent.Change(this.items, this.items.size))
  }

  fun add(item: T, position: Int) {
    items.add(position, item)
    subject.onNext(DataSourceEvent.Insert(item, position))
  }

  fun removeAt(position: Int) {
    items.removeAt(position)
    subject.onNext(DataSourceEvent.Remove(position))
  }

  override fun observeData(): Observable<DataSourceEvent> {
    return subject.asObservable()
      .startWith(DataSourceEvent.Load(this.items, this.items.size))
  }
}

class DataSourceAdapterObserver(
  private val adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>)
: Observer<DataSourceEvent> {
  override fun onNext(t: DataSourceEvent) {
    when (t) {
      is DataSourceEvent.Load -> adapter.notifyDataSetChanged()
      is DataSourceEvent.Change -> adapter.notifyDataSetChanged()
      is DataSourceEvent.Insert -> adapter.notifyItemInserted(t.position)
      is DataSourceEvent.Remove -> adapter.notifyItemRemoved(t.position)
    }
  }

  override fun onCompleted() {
  }

  override fun onError(e: Throwable) = logError(e)
}