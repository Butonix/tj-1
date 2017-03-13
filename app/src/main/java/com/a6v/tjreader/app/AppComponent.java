package com.a6v.tjreader.app;

import com.a6v.tjreader.activities.MainActivity;
import com.a6v.tjreader.net.NetworkModule;

import java.util.Set;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {
  AppModule.class,
  NetworkModule.class,
  SingletonsModule.class,
  FlavorModule.class
})
@Singleton
public interface AppComponent {
  Set<OnCreateApplicationCallback> getCreationCallbacks();

  MainActivity.Component mainActivityComponent(MainActivity.Module module);
}
