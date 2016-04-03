package com.a6v.tjreader.activities

import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import com.a6v.tjreader.R
import com.a6v.tjreader.extensions.getAppComponent
import com.a6v.tjreader.fragments.FeedFragment
import com.a6v.tjreader.fragments.ReadFragment
import com.a6v.tjreader.fragments.SavedFragment
import dagger.Subcomponent
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by alexey on 20.02.16.
 */
class MainActivity : BaseActivity() {
  lateinit var component: MainActivityComponent
  lateinit var drawerToggle: ActionBarDrawerToggle

  override fun initComponent() {
    component = getAppComponent().mainActivity(ActivityModule(this))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)
    drawerToggle = ActionBarDrawerToggle(this, drawer, toolbar, 0, 0)
    drawer.setDrawerListener(drawerToggle)
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

  override fun onBackPressed() {
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawers()
    } else {
      super.onBackPressed()
    }
  }

  fun showFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().replace(R.id.placeholder, fragment).commit()
  }
}

@Subcomponent(modules = arrayOf(ActivityModule::class))
@PerActivity
interface MainActivityComponent : AbstractActivityComponent {
  fun inject(activity: MainActivity)

  fun inject(fragment: FeedFragment)

  fun inject(fragment: SavedFragment)

  fun inject(fragment: ReadFragment)
}