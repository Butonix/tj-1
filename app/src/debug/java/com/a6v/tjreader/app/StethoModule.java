package com.a6v.tjreader.app;

import com.a6v.tjreader.net.NetworkModule;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import okhttp3.Interceptor;

@Module
public final class StethoModule {
  private StethoModule() {
    throw new AssertionError("No instances.");
  }

  @Provides
  @IntoSet
  public static OnCreateApplicationCallback provideOnCreateCallback() {
    return Stetho::initializeWithDefaults;
  }

  @Provides
  @IntoSet
  @Named(NetworkModule.NETWORK_INTERCEPTOR)
  public static Interceptor provideInterceptor() {
    return new StethoInterceptor();
  }
}
