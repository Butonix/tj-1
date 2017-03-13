package com.a6v.tjreader.app;

import com.a6v.tjreader.net.NetworkModule;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

@Module(includes = StethoModule.class)
public final class FlavorModule {
  private FlavorModule() {
    throw new AssertionError("No instances.");
  }

  @Provides
  @IntoSet
  @Named(NetworkModule.INTERCEPTOR)
  public static Interceptor provideInterceptors() {
    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT);
    interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
    return interceptor;
  }

  @Provides
  @IntoSet
  public static Timber.Tree provideDebugTree() {
    return new Timber.DebugTree();
  }
}
