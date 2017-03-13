package com.a6v.tjreader.fragments;

import android.support.v4.app.Fragment;

import com.a6v.tjreader.activities.AbstractActivityComponent;
import com.a6v.tjreader.activities.ActivityComponentProvider;

public class BaseFragment extends Fragment {
  protected <T extends AbstractActivityComponent> T getActivityComponent() {
    return ((ActivityComponentProvider<T>) getActivity()).getActivityComponent();
  }
}
