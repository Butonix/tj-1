package com.a6v.tjreader.widgets

import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.view.Menu
import android.view.MenuItem

class NavigationDrawer {
  val menu: Menu
  val drawer: DrawerLayout
  private var enabled: Boolean

  constructor(view: NavigationView, drawer: DrawerLayout, listener: (MenuItem) -> Unit):
    this(view, drawer, false, listener) {
  }

  constructor(view: NavigationView, drawer: DrawerLayout, enabled: Boolean,
    listener: (MenuItem) -> Unit)
  {
    this.menu = view.menu
    this.drawer = drawer
    this.enabled = enabled
    init(view, listener)
  }


  private fun init(menuView: NavigationView, listener: (MenuItem) -> Unit) {
    enable(enabled)
    menuView.setNavigationItemSelectedListener {
      it.isChecked = true
      drawer.closeDrawers()
      listener(it)
      true
    }
  }

  fun populateWith(idTitlePairs: List<Pair<Int, String>>) {
    enable(idTitlePairs.size > 1)//do not disable drawer with one item?
    if (!enabled) {
      return
    }
    for (i: Int in idTitlePairs.indices) {
      val idTitle = idTitlePairs[i]
      menu.add(0, idTitle.first, i, idTitle.second)
    }
    menu.setGroupCheckable(0, true, true)
  }

  fun removeItem(id: Int) {
    if (enabled) {
      menu.removeItem(id)
      enable(menu.size() != 1)
    }
  }

  fun setChecked(id: Int) {
    if (enabled) {
      menu.findItem(id).isChecked = true
    }
  }

  private fun enable(enabled: Boolean) {
    this.enabled = enabled
    if (enabled) {
      drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    } else {
      drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }
  }
}