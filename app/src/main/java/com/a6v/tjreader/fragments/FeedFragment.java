package com.a6v.tjreader.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.a6v.tjreader.R;
import com.a6v.tjreader.activities.MainActivity;
import com.a6v.tjreader.app.Logger;
import com.a6v.tjreader.datasources.FeedDataSource;
import com.a6v.tjreader.entities.ArticleFeedItem;
import com.a6v.tjreader.net.FeedSorting;
import com.a6v.tjreader.presenters.ArticleListPresenter;
import com.a6v.tjreader.services.DataSourceManager;
import com.a6v.tjreader.services.FeedType;
import com.jakewharton.rxbinding2.view.RxView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;

public class FeedFragment extends BaseFragment {
  private static final String ARG_FEED_TYPE = "feedType";
  private static final String ARG_SORTING = "sorting";
  private static final String PRESENTER_STATE = "presenter";

  private FeedType feedType;
  private FeedSorting sorting;
  @Inject DataSourceManager dataSourceManager;
  @Inject Logger logger;
  FeedDataSource source;
  private ArticleListPresenter listPresenter;
  //read button presenter

  @BindView(R.id.feed_swipe)
  SwipeRefreshLayout swipeLayout;

  @BindView(android.R.id.list)
  RecyclerView list;

  @BindView(R.id.error)
  TextView errorView;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    feedType = FeedType.values()[args.getInt(ARG_FEED_TYPE, FeedType.ALL.ordinal())];
    sorting = FeedSorting.values()[args.getInt(ARG_SORTING, FeedSorting.RECENT.ordinal())];
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.fragment_feed, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    if (source == null) {
      this.<MainActivity.Component>getActivityComponent().inject(this);
      source = dataSourceManager.getFeedDataSource(feedType, sorting);
      listPresenter = new ArticleListPresenter(source, logger);
    }
    ButterKnife.bind(this, view);
    Parcelable presenterState = savedInstanceState != null
      ? savedInstanceState.getParcelable(PRESENTER_STATE) : null;
    listPresenter.attachView(list, presenterState);
    new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
      @Override
      public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        ArticleFeedItem item = source.getItemAt(position);
        //delete
        source.removeItemAt(position);
      }
    }).attachToRecyclerView(list);
    listPresenter.setNextPageLoadingEnabled(false);
    if (savedInstanceState == null) {
      //check data source has data?
      source.loadNextPage()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(ignored -> {
          swipeLayout.setRefreshing(true);
          errorView.setVisibility(View.GONE);
        })
        .doOnError(error -> {
          swipeLayout.setRefreshing(false);
          errorView.setText("Network error\n Tap to retry");
          errorView.setVisibility(View.VISIBLE);
        })
        .retryWhen(errors -> errors.flatMapSingle(error ->
          RxView.clicks(errorView).singleOrError())
        )
        .subscribeWith(new DisposableSingleObserver<Boolean>() {
          @Override
          public void onSuccess(Boolean aBoolean) {
            swipeLayout.setRefreshing(false);
            listPresenter.setNextPageLoadingEnabled(true);
          }

          @Override
          public void onError(Throwable e) {
            logger.log(e);
            errorView.setText("Oops, something went wrong");
          }
        });//
    } else {
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    listPresenter.detach();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(PRESENTER_STATE, listPresenter.getInstanceState());
  }

  public static FeedFragment create(FeedType type, FeedSorting sorting) {
    FeedFragment fragment = new FeedFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_FEED_TYPE, type.ordinal());
    args.putInt(ARG_SORTING, sorting.ordinal());
    fragment.setArguments(args);
    return fragment;
  }
}
