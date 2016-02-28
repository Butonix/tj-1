package com.github.marwinxxii.tjournal.activities

import android.app.Activity
import android.support.v4.app.Fragment
import com.github.marwinxxii.tjournal.fragments.FragmentComponent
import com.github.marwinxxii.tjournal.fragments.FragmentModule
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Scope

/**
 * Created by alexey on 20.02.16.
 */

@PerActivity
@Subcomponent(modules = arrayOf(ActivityModule::class))
interface ActivityComponent {
  fun activity(): Activity

  fun plus(fragment: FragmentModule): FragmentComponent

  fun inject(activity: MainActivity)

  fun inject(activity: ReadActivity)
}

@Module
class ActivityModule(val activity: Activity) {
  @Provides @PerActivity
  fun activity(): Activity {
    return activity
  }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerActivity

interface ActivityComponentHolder {
  val component: ActivityComponent
}
