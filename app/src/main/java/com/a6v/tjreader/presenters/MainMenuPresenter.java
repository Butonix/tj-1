package com.a6v.tjreader.presenters;

import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import com.a6v.tjreader.R;

public class MainMenuPresenter {
  private static final int GRAVITY = GravityCompat.END;

  private DrawerLayout drawer;
  private NavigationView menu;

  public void attachView(View parent, NavigationView.OnNavigationItemSelectedListener listener) {
    attachView(
      (DrawerLayout) parent.findViewById(R.id.drawer),
      (NavigationView) parent.findViewById(R.id.drawer_menu),
      listener
    );
  }

  public void attachView(DrawerLayout drawer, NavigationView menu,
    NavigationView.OnNavigationItemSelectedListener listener)
  {
    this.drawer = drawer;
    this.menu = menu;
    menu.setNavigationItemSelectedListener(listener);
  }

  public void detach() {
    drawer = null;
    menu.setNavigationItemSelectedListener(null);
    menu = null;
  }

  public void open() {
    drawer.openDrawer(GRAVITY);
  }

  public boolean close() {
    if (drawer.isDrawerOpen(GRAVITY)) {
      drawer.closeDrawer(GRAVITY);
      return true;
    }
    return false;
  }

  public void setChecked(@IdRes int itemId) {
    menu.setCheckedItem(itemId);
  }
}
