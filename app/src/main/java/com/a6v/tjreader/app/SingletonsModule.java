package com.a6v.tjreader.app;

import com.google.gson.Gson;

import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

@Module
public final class SingletonsModule {
  private SingletonsModule() {
    throw new AssertionError("No instances.");
  }

  @Provides
  @Singleton
  public static Gson provideGson() {
    return new Gson();
  }

  @Provides
  @Singleton
  public static Logger provideLogger(Set<Timber.Tree> trees) {
    return new Logger(trees);
  }
}
