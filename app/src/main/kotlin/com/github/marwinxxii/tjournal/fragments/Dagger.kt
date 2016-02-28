package com.github.marwinxxii.tjournal.fragments

import android.support.v4.app.Fragment
import com.github.marwinxxii.tjournal.activities.ActivityComponent
import com.github.marwinxxii.tjournal.activities.ActivityComponentHolder
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Scope

/**
 * Created by alexey on 27.02.16.
 */
@Subcomponent(modules = arrayOf(FragmentModule::class))
interface FragmentComponent {
  fun inject(fragment: FeedFragment)

  fun inject(fragment: SavedFragment)

  fun inject(fragment: ReadFragment)

  fun inject(fragment: ArticleFragment)
}

@Module
class FragmentModule(val fragment: Fragment) {
  @Provides
  @PerFragment
  fun fragment(): Fragment {
    return fragment
  }
}

@Scope
annotation class PerFragment

fun Fragment.activityComponent(): ActivityComponent {
  return (activity as ActivityComponentHolder).component
}