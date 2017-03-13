package com.a6v.tjreader.widgets;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.a6v.pagingadapter.MessageViewHolder;

public class FeedMessageViewHolder extends RecyclerView.ViewHolder implements MessageViewHolder {
  private final TextView message;

  public FeedMessageViewHolder(View itemView) {
    super(itemView);
    message = (TextView) itemView.findViewById(android.R.id.message);
  }

  @Override
  public void bindMessage(@Nullable CharSequence text,
    @Nullable View.OnClickListener clickListener)
  {
    message.setText(text);
    itemView.setOnClickListener(clickListener);
  }
}
