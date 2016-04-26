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
import com.a6v.tjreader.service.FeedSorting
import com.a6v.tjreader.service.FeedType
import com.a6v.tjreader.widgets.NavigationDrawer
import dagger.Subcomponent
import kotlinx.android.synthetic.main.activity_main.*

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
    NavigationDrawer(drawer_menu, drawer, enabled = true) {
      when (it.itemId) {
        R.id.navi_feed_all -> showFeedFragment(FeedType.ALL, FeedSorting.RECENT)
        R.id.navi_feed_news -> showFeedFragment(FeedType.NEWS, FeedSorting.RECENT)
        R.id.navi_feed_articles -> showFeedFragment(FeedType.ARTICLES, FeedSorting.RECENT)
        R.id.navi_feed_offtop -> showFeedFragment(FeedType.OFF_TOPIC, FeedSorting.RECENT)
        R.id.navi_saved -> showFragment(SavedFragment())
        R.id.navi_read -> showFragment(ReadFragment())
      }
    }
    if (savedInstanceState == null) {
      showFeedFragment(FeedType.ALL, FeedSorting.RECENT)
      drawer_menu.setCheckedItem(R.id.navi_feed_all)
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

  fun showFeedFragment(feedType: FeedType, sorting: FeedSorting) {
    showFragment(FeedFragment.create(feedType, sorting))
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