package com.a6v.tjreader.app;

import java.util.Collection;

import io.reactivex.functions.Consumer;
import timber.log.Timber;

public class Logger {
  public Logger(Collection<Timber.Tree> trees) {
    Timber.plant(trees.toArray(new Timber.Tree[trees.size()]));
  }

  public void log(Throwable error) {
    Timber.e(error);
  }

  public Consumer<Throwable> getLogErrorAction() {
    return this::log;
  }
}
