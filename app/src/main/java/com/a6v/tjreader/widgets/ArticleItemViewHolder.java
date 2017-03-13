package com.a6v.tjreader.widgets;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.a6v.tjreader.R;
import com.a6v.tjreader.net.RawArticleItem;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArticleItemViewHolder extends RecyclerView.ViewHolder {

  @BindView(android.R.id.title)
  TextView title;

  @BindView(R.id.intro)
  TextView intro;

  public ArticleItemViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  public void bind(RawArticleItem item) {
    title.setText(item.title);
    intro.setText(item.intro);
  }
}
