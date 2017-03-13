package com.a6v.tjreader.datasources;

import android.support.annotation.Nullable;

import java.util.List;

public abstract class ListChange<T> {
  public final List<T> items;

  protected ListChange(List<T> items) {
    this.items = items;
  }

  public static class FullChange<T> extends ListChange<T> {
    public FullChange(List<T> items) {
      super(items);
    }
  }

  public static class Insert<T> extends ListChange<T> {
    public final int position;
    public final int count;

    public Insert(List<T> items, int position, int count) {
      super(items);
      this.position = position;
      this.count = count;
    }
  }

  public static class Remove<T> extends ListChange<T> {
    public final int position;
    public final int count;

    public Remove(List<T> items, int position, int count) {
      super(items);
      this.position = position;
      this.count = count;
    }
  }

  public static class Update<T> extends ListChange<T> {
    public final int position;
    public final int count;
    @Nullable public final Object payload;

    public Update(List<T> items, int position, int count, @Nullable Object payload) {
      super(items);
      this.position = position;
      this.count = count;
      this.payload = payload;
    }
  }
}
