package com.a6v.tjreader.datasources;

import android.support.annotation.Nullable;

import com.a6v.tjreader.app.Logger;
import com.a6v.tjreader.entities.ArticleFeedItem;
import com.a6v.tjreader.net.FeedSorting;
import com.a6v.tjreader.services.ArticleService;
import com.a6v.tjreader.services.FeedType;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

public class FeedDataSource {
  private static final int PAGE_SIZE = 10;

  private final FeedType type;
  private final FeedSorting sorting;
  private final ArticleService service;
  private final Logger logger;
  private final ArrayList<ArticleFeedItem> items = new ArrayList<>();
  private final PublishSubject<ListChange<ArticleFeedItem>> changes
    = PublishSubject.create();
  private boolean hasMore = true;
  private int currentPage = -1;
  @Nullable private Single<Boolean> page;

  public FeedDataSource(FeedType type, FeedSorting sorting, ArticleService service, Logger logger) {
    this.type = type;
    this.sorting = sorting;
    this.service = service;
    this.logger = logger;
  }

  public Single<Boolean> loadNextPage() {
    synchronized (items) {
      if (page != null) {
        return page;
      }
      if (!hasMore) {
        return Single.error(new NoSuchElementException("No more pages to load"));
      }
      final int pageNumber = ++currentPage;
      Single<Boolean> load = service.getArticles(pageNumber * PAGE_SIZE, PAGE_SIZE,
        type, sorting)
        .map(this::onPageLoaded)
        .doOnError(logger.getLogErrorAction())
        .cache();//FIXME retry
      page = load;
      return load;
    }
  }

  public ArticleFeedItem getItemAt(int position) {
    synchronized (items) {
      return items.get(position);
    }
  }

  public void removeItemAt(int position) {
    synchronized (items) {
      items.remove(position);
      changes.onNext(new ListChange.Remove<>(items, position, 1));
    }
  }

  public Observable<ListChange<ArticleFeedItem>> changes() {
    synchronized (items) {
      return changes.startWith(new ListChange.FullChange<>(items));
    }
  }

  boolean onPageLoaded(List<ArticleFeedItem> pageItems) {
    synchronized (items) {
      page = null;
      final int insertPosition = items.size();
      items.addAll(pageItems);
      final int count = pageItems.size();
      hasMore = count >= PAGE_SIZE;
      changes.onNext(new ListChange.Insert<>(items, insertPosition, count));
      return hasMore;
    }
  }
}
