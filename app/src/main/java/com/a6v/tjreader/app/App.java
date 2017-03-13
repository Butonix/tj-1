package com.a6v.tjreader.app;

import android.app.Application;

public class App extends Application {
  private static AppComponent component;

  @Override
  public void onCreate() {
    super.onCreate();
    component = DaggerAppComponent.builder()
      .appModule(new AppModule(this))
      .build();
    for (OnCreateApplicationCallback callback : component.getCreationCallbacks()) {
      callback.onCreateApplication(this);
    }
  }

  public AppComponent getComponent() {
    return component;
  }
}
