package com.github.marwinxxii.tjournal.activities

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.view.Menu
import android.view.MenuItem
import com.github.marwinxxii.tjournal.EventBus
import com.github.marwinxxii.tjournal.R
import com.github.marwinxxii.tjournal.extensions.getAppComponent
import com.github.marwinxxii.tjournal.fragments.ArticleFragment
import com.github.marwinxxii.tjournal.fragments.LoadArticleRequestEvent
import com.github.marwinxxii.tjournal.service.ArticlesDAO
import dagger.Subcomponent
import kotlinx.android.synthetic.main.activity_read.*
import org.jetbrains.anko.toast
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by alexey on 23.02.16.
 */
class ReadActivity : BaseActivity() {
  lateinit var component: ReadActivityComponent
  @Inject lateinit var cache: ArticlesDAO
  @Inject lateinit var eventBus: EventBus
  val articleIds: MutableList<Int> = mutableListOf()
  lateinit var articleMenu: ArticlesMenu
  var currentArticleId = 0

  override fun initComponent() {
    component = getAppComponent().readActivity(ActivityModule(this))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_read)
    setSupportActionBar(toolbar)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    articleMenu = ArticlesMenu(drawer_menu, drawer) {
      loadArticle(it.itemId)
    }
    component.inject(this)
    //TODO check for empty list?
    cache.getReadyArticlesIds()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle<List<Pair<Int, String>>>())
      .subscribe {
        articleIds.addAll(it.map { it.first } )//TODO optimize
        articleMenu.populateWith(it)
        loadNextArticle()
      }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.read, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item?.itemId) {
      R.id.read -> {
        val id = currentArticleId
        cache.markArticleRead(id)
          .subscribeOn(Schedulers.computation())
          .subscribe()
        articleIds.remove(id)
        articleMenu.removeItem(id)
        if (articleIds.size > 0) {
          loadNextArticle()
        } else {
          toast("No more articles")
          finish()
        }
      }
      android.R.id.home -> finish()//TODO NavUtils.navigateUpFromSameTask
    }
    return true
  }

  private fun loadNextArticle() {
    val id = articleIds[0]
    loadArticle(id)
    articleMenu.setChecked(id)
  }

  private fun loadArticle(id: Int) {
    currentArticleId = id
    eventBus.post(LoadArticleRequestEvent(id))
  }
}

@Subcomponent(modules = arrayOf(ActivityModule::class))
@PerActivity
interface ReadActivityComponent : AbstractActivityComponent {
  fun inject(activity: ReadActivity)

  fun inject(fragment: ArticleFragment)
}

class ArticlesMenu {
  val menu: Menu
  val drawer: DrawerLayout
  private var enabled: Boolean = false

  constructor(view: NavigationView, drawer: DrawerLayout, listener: (MenuItem) -> Unit) {
    this.menu = view.menu
    this.drawer = drawer
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

  fun populateWith(articleIdTitlePairs: List<Pair<Int, String>>) {
    enable(articleIdTitlePairs.size > 1)//do not disable drawer with one article?
    if (!enabled) {
      return
    }
    for (i: Int in articleIdTitlePairs.indices) {
      val idTitle = articleIdTitlePairs[i]
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