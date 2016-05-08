package com.a6v.tjreader.widgets

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.a6v.tjreader.R
import kotlinx.android.synthetic.main.widget_list_progress.view.*

const val loaderViewType = 100
const val messageViewType = 200

class ProgressLoadingAdapter<T : RecyclerView.ViewHolder> : RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private val adapter: RecyclerView.Adapter<T>
  private val onLoad: (ProgressLoadingAdapter<T>) -> Unit
  private val mainThreadHandler = Handler(Looper.getMainLooper())
  private var state = State.IDLE
  private var inflater: LayoutInflater? = null
  private var itemPosition = -1
  private var itemCount = 0

  private var message: CharSequence = ""

  constructor(adapter: RecyclerView.Adapter<T>, onLoad: (ProgressLoadingAdapter<T>) -> Unit) {
    this.adapter = adapter
    adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
      override fun onChanged() {
        this@ProgressLoadingAdapter.notifyDataSetChanged()
      }

      override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        this@ProgressLoadingAdapter.notifyItemRangeRemoved(positionStart, itemCount)
      }
    })
    this.onLoad = onLoad
  }

  override fun getItemCount(): Int {
    val count = when (state) {
      State.IDLE -> 0
      State.READY_TO_LOAD, State.LOADING, State.MESSAGE -> 1
    }
    itemCount = adapter.itemCount + count
    return itemCount
  }

  override fun getItemViewType(position: Int): Int {
    if (position == itemCount - 1) {
      when (state) {
        State.READY_TO_LOAD, State.LOADING -> {
          return loaderViewType
        }
        State.MESSAGE -> {
          return messageViewType
        }
      }
    }
    val itemViewType = adapter.getItemViewType(position)
    return itemViewType
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    if (holder is LoaderViewHolder) {
      itemPosition = position
      if (state == State.READY_TO_LOAD) {
        state = State.LOADING
        mainThreadHandler.post {
          onLoad(this)
        }
      }
    } else if (holder is MessageViewHolder) {
      holder.bind(message)
      itemPosition = position
    } else {
      adapter.onBindViewHolder(holder as T, position)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    if (inflater == null) {
      inflater = LayoutInflater.from(parent.context)
    }
    val i = inflater!!
    when (viewType) {
      loaderViewType -> {
        return LoaderViewHolder(i.inflate(R.layout.widget_list_progress, parent, false))
      }
      messageViewType -> {
        return MessageViewHolder(i.inflate(R.layout.widget_list_message, parent, false))
      }
      else -> return adapter.onCreateViewHolder(parent, viewType)
    }
  }

  fun startLoad() {
    enableNextPageLoading(true)
    notifyDataSetChanged()
  }

  fun enableNextPageLoading(enable: Boolean) {
    state = if (enable) State.READY_TO_LOAD else State.IDLE
    //notify about change?
  }

  fun showMessage(message: CharSequence) {
    this.message = message
    state = State.MESSAGE
    if (itemPosition >= 0) {
      notifyItemChanged(itemPosition)
    } else {
      //TODO notify item inserted?
      notifyDataSetChanged()
    }
  }

  fun setLoaded(enableNextPageLoad: Boolean) {
    state = if (enableNextPageLoad) State.READY_TO_LOAD else State.IDLE
    if (itemPosition >= 0) {
      //notifyItemRemoved(itemPosition)
      itemPosition = -1
    }
  }
}

private enum class State {
  IDLE, READY_TO_LOAD, LOADING, MESSAGE
}

private class MessageViewHolder : RecyclerView.ViewHolder {
  private val messageView: TextView

  constructor(itemView: View) : super(itemView) {
    messageView = itemView as TextView
  }

  fun bind(message: CharSequence) {
    messageView.text = message
  }
}

private class LoaderViewHolder : RecyclerView.ViewHolder {
  private val loader: ProgressBar

  constructor(itemView: View) : super(itemView) {
    loader = itemView.list_progress
  }
}