package com.a6v.tjreader.app;

import com.a6v.tjreader.net.NetworkModule;

import java.util.Collections;
import java.util.Set;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import okhttp3.Interceptor;
import timber.log.Timber;

@Module
public final class FlavorModule {
  private FlavorModule() {
    throw new AssertionError("No instances");
  }

  //temporary
  @Provides
  @ElementsIntoSet
  public static Set<OnCreateApplicationCallback> provideAppCallbacks() {
    return Collections.emptySet();
  }

  //temporary
  @Provides
  public static Set<Timber.Tree> provideLoggers() {
    return Collections.emptySet();
  }

  @Provides
  @Named(NetworkModule.INTERCEPTOR)
  public static Set<Interceptor> provideInterceptors() {
    return Collections.emptySet();
  }

  @Provides
  @Named(NetworkModule.NETWORK_INTERCEPTOR)
  public static Set<Interceptor> provideNetworkInterceptors() {
    return Collections.emptySet();
  }
}
