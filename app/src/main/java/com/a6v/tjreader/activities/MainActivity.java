package com.a6v.tjreader.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.a6v.tjreader.R;
import com.a6v.tjreader.app.Logger;
import com.a6v.tjreader.fragments.FeedFragment;
import com.a6v.tjreader.net.FeedSorting;
import com.a6v.tjreader.presenters.MainMenuPresenter;
import com.a6v.tjreader.services.ArticleService;
import com.a6v.tjreader.services.DataSourceManager;
import com.a6v.tjreader.services.FeedType;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Provides;
import dagger.Subcomponent;

public class MainActivity extends BaseActivity
  implements NavigationView.OnNavigationItemSelectedListener, ActivityComponentProvider
{
  private static final FeedSorting DEFAULT_SORTING = FeedSorting.RECENT;

  @BindView(R.id.toolbar)
  Toolbar toolbar;

  private AbstractActivityComponent component;
  private MainMenuPresenter menuPresenter;

  @Inject DataSourceManager dataSourceManager;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    component = getAppComponent().mainActivityComponent(new Module(this,
      getLastCustomNonConfigurationInstance()));
    setContentView(R.layout.activity_feed);
    ButterKnife.bind(this);
    toolbar.inflateMenu(R.menu.feed);
    menuPresenter = new MainMenuPresenter();
    menuPresenter.attachView(findViewById(R.id.drawer), this);
    toolbar.setOnMenuItemClickListener(item -> {
      menuPresenter.open();
      return true;
    });

    if (savedInstanceState == null) {
      menuPresenter.setChecked(R.id.navi_feed_all);
      showFeedFragment(FeedType.ALL, DEFAULT_SORTING);
    }
  }

  @Override
  public void onBackPressed() {
    if (!menuPresenter.close()) {
      super.onBackPressed();
    }
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    return dataSourceManager;
  }

  @Override
  public AbstractActivityComponent getActivityComponent() {
    return component;
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    menuPresenter.close();
    switch (item.getItemId()) {
      case R.id.navi_feed_all:
        showFeedFragment(FeedType.ALL, DEFAULT_SORTING);
        break;
      case R.id.navi_feed_news:
        showFeedFragment(FeedType.NEWS, DEFAULT_SORTING);
        break;
      case R.id.navi_feed_articles:
        showFeedFragment(FeedType.ARTICLES, DEFAULT_SORTING);
        break;
      case R.id.navi_feed_offtop:
        showFeedFragment(FeedType.OFF_TOPIC, DEFAULT_SORTING);
        break;
      case R.id.navi_saved:
        break;
      case R.id.navi_read:
        break;
    }
    return true;
  }

  private void showFragment(Fragment fragment) {
    getSupportFragmentManager()
      .beginTransaction()
      .replace(R.id.placeholder, fragment)
      .commit();
  }

  private void showFeedFragment(FeedType type, FeedSorting sorting) {
    showFragment(FeedFragment.create(type, sorting));
  }

  @Subcomponent(modules = Module.class)
  @PerActivity
  public interface Component extends AbstractActivityComponent {
    void inject(FeedFragment fragment);
  }

  @dagger.Module
  public static class Module extends ActivityModule {
    private final DataSourceManager dataSourceManager;

    public Module(Activity activity, Object lastCustomNonConfigurationInstance) {
      super(activity);
      if (lastCustomNonConfigurationInstance != null) {
        dataSourceManager = (DataSourceManager) lastCustomNonConfigurationInstance;
      } else {
        dataSourceManager = null;
      }
    }

    @Provides
    @PerActivity
    public DataSourceManager provideDataSourceManager(ArticleService articleService,
      Logger logger)
    {
      if (dataSourceManager == null) {
        return new DataSourceManager(articleService, logger);
      }
      return dataSourceManager;
    }
  }
}
