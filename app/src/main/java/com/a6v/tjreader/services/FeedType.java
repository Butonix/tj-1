package com.a6v.tjreader.services;

public enum FeedType {
  ALL(0), NEWS(1), OFF_TOPIC(2), VIDEO(3), ARTICLES(4);

  public final int value;

  FeedType(int value) {
    this.value = value;
  }
}
