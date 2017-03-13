package com.a6v.tjreader.services;

import com.a6v.tjreader.entities.ArticleFeedItem;
import com.a6v.tjreader.net.FeedSorting;
import com.a6v.tjreader.net.TJournalAPI;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;

@Singleton
public class ArticleService {
  private final TJournalAPI api;

  @Inject
  public ArticleService(TJournalAPI api) {
    this.api = api;
  }

  public Single<List<ArticleFeedItem>> getArticles(
    int offset,
    int count,
    FeedType type,
    FeedSorting sorting
  )
  {
    return api.getNews(offset, count, type.value, sorting)
      .doOnSuccess(items -> {
        for (ArticleFeedItem item : items) {
          final String intro = item.intro;
          item.intro = ArticleHtmlParser.getIntroText(intro);
          item.introHtml = intro;
        }
      });//TODO filter read items
  }
}
