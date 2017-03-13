package com.a6v.tjreader.services;

import android.support.annotation.UiThread;

import com.a6v.tjreader.app.Logger;
import com.a6v.tjreader.datasources.FeedDataSource;
import com.a6v.tjreader.net.FeedSorting;

public class DataSourceManager {
  private final ArticleService articleService;
  private final Logger logger;

  public DataSourceManager(ArticleService articleService, Logger logger) {
    this.articleService = articleService;
    this.logger = logger;
  }

  @UiThread
  public FeedDataSource getFeedDataSource(FeedType type, FeedSorting sorting) {
    return new FeedDataSource(type, sorting, articleService, logger);
  }
}
