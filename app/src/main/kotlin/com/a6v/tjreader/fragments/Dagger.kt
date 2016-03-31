package com.a6v.tjreader.fragments

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
  fun fragment(): BaseFragment
}

@Module
class FragmentModule(val fragment: BaseFragment) {
  @Provides
  @PerFragment
  fun fragment(): BaseFragment {
    return fragment
  }
}

@Scope
annotation class PerFragment