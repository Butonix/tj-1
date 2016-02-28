package com.github.marwinxxii.tjournal.activities

import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import com.github.marwinxxii.tjournal.R
import com.github.marwinxxii.tjournal.extensions.getAppComponent
import com.github.marwinxxii.tjournal.fragments.FeedFragment
import com.github.marwinxxii.tjournal.fragments.ReadFragment
import com.github.marwinxxii.tjournal.fragments.SavedFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by alexey on 20.02.16.
 */
class MainActivity : AppCompatActivity(), ActivityComponentHolder {
  lateinit override var component: ActivityComponent
  lateinit var drawerToggle: ActionBarDrawerToggle

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)
    drawerToggle = ActionBarDrawerToggle(this, drawer, toolbar, 0, 0)
    drawer.setDrawerListener(drawerToggle)
    component = getAppComponent().plus(ActivityModule(this))
    component.inject(this)
    drawer_menu.setNavigationItemSelectedListener({
      it.isChecked = true
      when (it.itemId) {
        R.id.navi_feed -> showFragment(FeedFragment())
        R.id.navi_saved -> showFragment(SavedFragment())
        R.id.navi_read -> showFragment(ReadFragment())
      }
      drawer.closeDrawers()
      true
    })
    if (savedInstanceState == null) {
      showFragment(FeedFragment())
      drawer_menu.setCheckedItem(R.id.navi_feed)
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    drawerToggle.syncState()
  }

  override fun onConfigurationChanged(newConfig: Configuration?) {
    super.onConfigurationChanged(newConfig)
    drawerToggle.onConfigurationChanged(newConfig)
  }

  fun showFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().replace(R.id.placeholder, fragment).commit()
  }
}