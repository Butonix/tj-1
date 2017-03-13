package com.a6v.tjreader.activities;

public interface ActivityComponentProvider<T extends AbstractActivityComponent> {
  T getActivityComponent();
}
