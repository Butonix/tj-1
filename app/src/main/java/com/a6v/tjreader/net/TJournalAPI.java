package com.a6v.tjreader.net;

import com.a6v.tjreader.entities.ArticleFeedItem;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TJournalAPI {
  @GET("club")
  Single<List<ArticleFeedItem>> getNews(
    @Query("offset") int offset,
    @Query("count") int count,
    @Query("type") int type,
    @Query("sortMode") FeedSorting sorting
  );
}
