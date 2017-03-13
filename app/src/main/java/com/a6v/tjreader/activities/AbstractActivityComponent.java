package com.a6v.tjreader.activities;

import com.a6v.tjreader.fragments.FeedFragment;

import dagger.Subcomponent;

@Subcomponent(modules = ActivityModule.class)
@PerActivity
public interface AbstractActivityComponent {
}
