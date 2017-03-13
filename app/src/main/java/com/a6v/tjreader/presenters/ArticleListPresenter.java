package com.a6v.tjreader.presenters;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.a6v.pagingadapter.PagingAdapterDecorator;
import com.a6v.pagingadapter.rx2.RxPagingAdapter;
import com.a6v.tjreader.app.Logger;
import com.a6v.tjreader.datasources.FeedDataSource;
import com.a6v.tjreader.datasources.ListChange;
import com.a6v.tjreader.entities.ArticleFeedItem;
import com.a6v.tjreader.utils.Utils;
import com.a6v.tjreader.widgets.ArticleItemAdapter;
import com.a6v.tjreader.widgets.ListPageState;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class ArticleListPresenter {
  public static final String PAGE_STATE = "pageState";
  private final FeedDataSource source;
  final Logger logger;

  private RecyclerView view;
  private PagingAdapterDecorator pagingAdapter;
  @Nullable CompositeDisposable disposables;
  ListPageState pageState = ListPageState.IDLE;

  public ArticleListPresenter(FeedDataSource source, Logger logger) {
    this.source = source;
    this.logger = logger;
  }

  public void attachView(RecyclerView view, Parcelable savedInstanceState) {
    this.view = view;
    Context context = view.getContext();
    view.setHasFixedSize(true);
    view.setLayoutManager(new LinearLayoutManager(context));
    ArticleItemAdapter adapter = new ArticleItemAdapter(LayoutInflater.from(context));
    pagingAdapter = PagingAdapterDecorator.withStableIds(
      adapter,
      ArticleItemAdapter.PROGRESS_VIEW_TYPE,
      ArticleItemAdapter.PROGRESS_ITEM_ID,
      ArticleItemAdapter.MESSAGE_VIEW_TYPE,
      ArticleItemAdapter.MESSAGE_ITEM_ID
    );
    view.setAdapter(pagingAdapter);
    disposables = new CompositeDisposable();
    disposables.add(
      source.changes()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableObserver<ListChange<ArticleFeedItem>>() {
          @Override
          public void onNext(ListChange<ArticleFeedItem> change) {
            adapter.applyChange(change);
          }

          @Override
          public void onError(Throwable e) {
            logger.log(e);
          }

          @Override
          public void onComplete() {
          }
        })
    );
    disposables.add(
      RxPagingAdapter.progressShown(pagingAdapter)
        .flatMapSingle(ignored -> {
            pageState = ListPageState.LOADING;
            return source.loadNextPage()
              .doOnSuccess(pagingAdapter::hideProgress)
              .doOnError(i -> {
                pageState = ListPageState.ERROR;
                pagingAdapter.showMessage("Network error");
              })
              .retryWhen(errors -> errors.flatMapSingle(error -> RxPagingAdapter
                .messageClicks(pagingAdapter)
                .firstOrError()
              ));
          }
        )
        .takeWhile(hasMore -> hasMore)
        .subscribeWith(new DisposableObserver<Boolean>() {
          @Override
          public void onNext(Boolean hasMore) {
            pageState = ListPageState.IDLE;
          }

          @Override
          public void onError(Throwable e) {
            logger.log(e);
          }

          @Override
          public void onComplete() {
          }
        })
    );
    if (savedInstanceState != null) {
      Bundle bundle = (Bundle) savedInstanceState;
      pageState = Utils.readEnumValueFromBundle(bundle, PAGE_STATE, ListPageState.values(),
        ListPageState.IDLE);
      switch (pageState) {
        case LOADING:
          pagingAdapter.showProgress();
          break;
        case ERROR:
          pagingAdapter.showMessage("Network error");
          break;
        case IDLE:
          break;
      }
    }
  }

  public void detach() {
    view = null;
    if (disposables != null) {
      disposables.dispose();
      disposables = null;
    }
  }

  public Parcelable getInstanceState() {
    Bundle bundle = new Bundle();
    bundle.putInt(PAGE_STATE, pageState.ordinal());
    return bundle;
  }

  public void setNextPageLoadingEnabled(boolean enabled) {
    pagingAdapter.setShowProgressEnabled(enabled);
  }
}
