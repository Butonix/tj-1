package com.github.marwinxxii.tjournal.fragments

import android.support.v4.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Scope

/**
 * Created by alexey on 27.02.16.
 */
@PerFragment
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