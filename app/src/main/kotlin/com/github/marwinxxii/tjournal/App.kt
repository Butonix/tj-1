package com.github.marwinxxii.tjournal

import android.app.Application
import com.github.marwinxxii.tjournal.activities.ActivityModule
import com.github.marwinxxii.tjournal.activities.MainActivityComponent
import com.github.marwinxxii.tjournal.activities.ReadActivityComponent
import com.github.marwinxxii.tjournal.network.NetworkModule
import com.github.marwinxxii.tjournal.service.ArticlesModule
import com.github.marwinxxii.tjournal.service.DBModule
import dagger.Component
import dagger.Module
import dagger.Provides
import rx.Observable
import rx.subjects.PublishSubject
import javax.inject.Singleton

/**
 * Created by alexey on 20.02.16.
 */
class App : Application() {
  lateinit var component: AppComponent

  override fun onCreate() {
    super.onCreate()
    component = DaggerAppComponent.builder()
      .appModule(AppModule(this))
      .build()
  }
}

class EventBus {
  private val subject: PublishSubject<Any> = PublishSubject.create()

  fun <T> observe(cl: Class<T>): Observable<T> {
    return subject.ofType(cl);
  }

  fun post(event: Any) {
    subject.onNext(event)
  }
}

@Singleton
@Component(modules = arrayOf(
  AppModule::class,
  NetworkModule::class,
  DBModule::class,
  ArticlesModule::class,
  ImageLoaderModule::class)
)
interface AppComponent {
  fun app(): App

  fun mainActivity(activityModule: ActivityModule): MainActivityComponent

  fun readActivity(activityModule: ActivityModule): ReadActivityComponent
}

@Module
class AppModule(val app: App) {
  @Provides
  @Singleton
  fun app(): App {
    return app
  }

  @Provides
  @Singleton
  fun provideEventBus(): EventBus {
    return EventBus()
  }
}