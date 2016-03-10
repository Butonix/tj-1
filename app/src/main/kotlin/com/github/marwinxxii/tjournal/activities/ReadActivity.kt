package com.github.marwinxxii.tjournal.activities

import android.os.Bundle
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

  override fun initComponent() {
    component = getAppComponent().readActivity(ActivityModule(this))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_read)
    setSupportActionBar(toolbar)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    drawer_menu.setNavigationItemSelectedListener {
      it.isChecked = true
      eventBus.post(LoadArticleRequestEvent(it.itemId))//TODO method
      drawer.closeDrawers()
      true
    }
    component.inject(this)
    cache.getReadyArticlesIds()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe {
        articleIds.addAll(it.map { it.first } )//TODO optimize
        loadNextArticle()
        if (it.size > 1) {//do not disable drawer with one article?
          initMenu(it)
        }
      }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.read, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item?.itemId) {
      R.id.read -> {
        cache.markArticleRead(articleIds[0])
          .subscribeOn(Schedulers.computation())
          .subscribe()
        articleIds.removeAt(0)
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
    eventBus.post(LoadArticleRequestEvent(articleIds[0]))
  }

  private fun initMenu(articles: List<Pair<Int, String>>) {
    val menu = drawer_menu.menu
    for (i: Int in articles.indices) {
      val idTitle = articles[i]
      menu.add(0, idTitle.first, i, idTitle.second).isChecked = i == 0
    }
    menu.setGroupCheckable(0, true, true)
    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
  }
}

@Subcomponent(modules = arrayOf(ActivityModule::class))
@PerActivity
interface ReadActivityComponent : AbstractActivityComponent {
  fun inject(activity: ReadActivity)

  fun inject(fragment: ArticleFragment)
}