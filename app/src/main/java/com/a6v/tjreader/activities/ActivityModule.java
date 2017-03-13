package com.a6v.tjreader.activities;

import android.app.Activity;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {
  private final Activity activity;

  public ActivityModule(Activity activity) {
    this.activity = activity;
  }

  @Provides
  @PerActivity
  public Activity provideActivity() {
    return activity;
  }
}
