package com.a6v.tjreader.net;

import android.support.annotation.Nullable;

import com.a6v.tjreader.entities.ArticleCoverPhoto;
import com.a6v.tjreader.entities.ArticleExternalSource;

import java.util.Date;

public class RawArticleItem {
  public int id;
  public String title;
  public String url;
  public String intro;
  //public Date date;
  public long date;
  public int commentsCount;
  public Object likes;
  @Nullable
  public ArticleCoverPhoto cover;
  @Nullable
  public ArticleExternalSource externalLink;
  public boolean hasFullText;
}
