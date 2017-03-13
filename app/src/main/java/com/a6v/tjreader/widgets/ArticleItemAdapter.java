package com.a6v.tjreader.widgets;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.a6v.tjreader.R;
import com.a6v.tjreader.datasources.ListChange;
import com.a6v.tjreader.datasources.ListChange.Remove;
import com.a6v.tjreader.datasources.ListChange.Insert;
import com.a6v.tjreader.datasources.ListChange.Update;
import com.a6v.tjreader.entities.ArticleFeedItem;

import java.util.Collections;
import java.util.List;

public class ArticleItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  public static final int PROGRESS_VIEW_TYPE = 0, MESSAGE_VIEW_TYPE = 1, ITEM_VIEW_TYPE = 2;
  public static final long PROGRESS_ITEM_ID = 0, MESSAGE_ITEM_ID = 1;

  private final LayoutInflater inflater;
  private List<ArticleFeedItem> items = Collections.emptyList();

  public ArticleItemAdapter(LayoutInflater inflater) {
    this.inflater = inflater;
    setHasStableIds(true);
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case PROGRESS_VIEW_TYPE:
        return new FeedProgressViewHolder(inflate(R.layout.widget_list_progress, parent));
      case MESSAGE_VIEW_TYPE:
        return new FeedMessageViewHolder(inflate(R.layout.widget_list_message, parent));
      case ITEM_VIEW_TYPE:
      default:
        return new ArticleItemViewHolder(inflate(R.layout.widget_article_item, parent));
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    ((ArticleItemViewHolder) holder).bind(items.get(position));
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  @Override
  public int getItemViewType(int position) {
    return ITEM_VIEW_TYPE;
  }

  @Override
  public long getItemId(int position) {
    return items.get(position).id * 10L;
  }

  public void applyChange(ListChange<ArticleFeedItem> change) {
    this.items = change.items;
    if (change instanceof ListChange.FullChange) {
      notifyDataSetChanged();
    } else if (change instanceof Insert) {
      Insert insert = (Insert) change;
      notifyItemRangeInserted(insert.position, insert.count);
    } else if (change instanceof ListChange.Remove) {
      Remove remove = (Remove) change;
      notifyItemRangeRemoved(remove.position, remove.count);
    } else if (change instanceof Update) {
      Update update = (Update) change;
      if (update.payload != null) {
        notifyItemChanged(update.position, update.payload);
      } else {
        notifyItemChanged(update.position);
      }
    }
  }

  private View inflate(@LayoutRes int layoutRes, ViewGroup parent) {
    return inflater.inflate(layoutRes, parent, false);
  }
}
