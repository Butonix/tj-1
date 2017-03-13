package com.a6v.tjreader.activities;

import android.support.v7.app.AppCompatActivity;

import com.a6v.tjreader.app.App;
import com.a6v.tjreader.app.AppComponent;

public class BaseActivity extends AppCompatActivity {
  protected App getApp() {
    return (App) getApplication();
  }

  protected AppComponent getAppComponent() {
    return getApp().getComponent();
  }
}
