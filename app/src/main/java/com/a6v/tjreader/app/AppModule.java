package com.a6v.tjreader.app;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
  private final App app;

  public AppModule(App app) {
    this.app = app;
  }

  @Provides
  public App provideApp() {
    return app;
  }
}
