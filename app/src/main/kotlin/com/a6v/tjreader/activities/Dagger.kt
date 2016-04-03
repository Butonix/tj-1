package com.a6v.tjreader.activities

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Scope

/**
 * Created by alexey on 20.02.16.
 */

@PerActivity
@Subcomponent(modules = arrayOf(ActivityModule::class))
interface AbstractActivityComponent {
  fun activity(): BaseActivity
}

@Module
class ActivityModule(val activity: BaseActivity) {
  @Provides @PerActivity
  fun activity(): BaseActivity {
    return activity
  }

  @Provides
  @PerActivity
  fun provideRetainedState(activity: BaseActivity): ActivityRetainedState {
    val last = activity.lastCustomNonConfigurationInstance
    if (last != null && last is ActivityRetainedState) {
      return last
    }
    return ActivityRetainedState()
  }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerActivity
