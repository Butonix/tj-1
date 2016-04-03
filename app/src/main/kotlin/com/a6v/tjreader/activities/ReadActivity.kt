package com.a6v.tjreader.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import com.a6v.tjreader.EventBus
import com.a6v.tjreader.R
import com.a6v.tjreader.entities.Article
import com.a6v.tjreader.extensions.getAppComponent
import com.a6v.tjreader.extensions.isActivityResolved
import com.a6v.tjreader.fragments.ArticleFragment
import com.a6v.tjreader.db.ArticlesDAO
import com.a6v.tjreader.widgets.ArticleLoadedEvent
import com.a6v.tjreader.widgets.ArticleWebViewController
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.android.synthetic.main.activity_read.*
import org.jetbrains.anko.share
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
  @Inject lateinit var webViewController: ArticleWebViewController
  @Inject lateinit var eventBus: EventBus
  val articleIds: MutableList<Int> = mutableListOf()
  lateinit var articleMenu: ArticlesMenu
  var currentArticle: Article? = null

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
    eventBus.observe(ArticleLoadedEvent::class.java)
      .compose(bindToLifecycle<ArticleLoadedEvent>())
      .subscribe {
        currentArticle = it.article
        supportInvalidateOptionsMenu()
      }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.read, menu)
    return true
  }

  override fun onPrepareOptionsMenu(menu: Menu): Boolean {
    val article = currentArticle
    if (article != null) {
      val appIntent = createTjAppIntent(article)
      if (isActivityResolved(appIntent)) {
        menu.findItem(R.id.menu_open_tjournal).intent = appIntent
      } else {
        menu.removeItem(R.id.menu_open_tjournal)
      }
      val webIntent = createWebIntent(article)
      if (isActivityResolved(webIntent)) {
        menu.findItem(R.id.menu_open_site).intent = webIntent
      } else {
        menu.removeItem(R.id.menu_open_site)
      }
    } else {
      menu.removeItem(R.id.menu_share)
      menu.removeItem(R.id.menu_open_tjournal)
      menu.removeItem(R.id.menu_open_site)
    }
    return super.onPrepareOptionsMenu(menu)
  }

  private fun createTjAppIntent(article: Article): Intent {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.preview.url))
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.setPackage("ru.kraynov.app.tjournal")
    return intent
  }

  private fun createWebIntent(article: Article): Intent {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.preview.url))
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    return intent
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val article = currentArticle
    if (article != null) {
      when (item.itemId) {
        R.id.menu_read -> {
          val id = article.preview.id
          cache.markArticleRead(id)
            .subscribeOn(Schedulers.computation())
            .subscribe()
          var index = articleIds.indexOf(id)
          articleIds.removeAt(index)
          articleMenu.removeItem(id)
          if (articleIds.size > 0) {
            if (index > articleIds.size) {
              index = articleIds.size - 1
            }
            loadArticle(articleIds[index])//item at index is replaced with new one
          } else {
            toast("No more articles")
            finish()
            return true
          }
        }
        R.id.menu_share -> {
          share(article.preview.url)
          return true
        }
      }
    }
    when (item.itemId) {
      android.R.id.home -> {
        finish()//TODO NavUtils.navigateUpFromSameTask
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onBackPressed() {
    if (drawer.isDrawerOpen(GravityCompat.END)) {
      drawer.closeDrawers()
    } else {
      super.onBackPressed()
    }
  }

  private fun loadNextArticle() {
    val id: Int
    if (intent.hasExtra(EXTRA_ARTICLE_ID)) {
      id = intent.getIntExtra(EXTRA_ARTICLE_ID, 0)
    } else {
      id = articleIds[0]
    }
    loadArticle(id)
    articleMenu.setChecked(id)
  }

  private fun loadArticle(id: Int) {
    webViewController.loadArticle(id)
  }

  companion object {
    private const val EXTRA_ARTICLE_ID = "articleId"

    fun getIntent(context: Context, articleId: Int): Intent {
      val intent = Intent(context, ReadActivity::class.java)
      intent.putExtra(EXTRA_ARTICLE_ID, articleId)
      return intent
    }
  }
}

@Subcomponent(modules = arrayOf(ActivityModule::class, WebViewModule::class))
@PerActivity
interface ReadActivityComponent : AbstractActivityComponent {
  fun inject(activity: ReadActivity)

  fun inject(fragment: ArticleFragment)
}

@Module
class WebViewModule {
  @Provides
  @PerActivity
  fun provideWebView(activity: BaseActivity): WebView {
    return activity.findViewById(R.id.webView) as WebView
  }
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