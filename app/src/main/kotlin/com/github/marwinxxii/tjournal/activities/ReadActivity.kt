package com.github.marwinxxii.tjournal.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.github.marwinxxii.tjournal.R
import com.github.marwinxxii.tjournal.extensions.getAppComponent
import com.github.marwinxxii.tjournal.service.ArticlesDAO
import kotlinx.android.synthetic.main.activity_read.*
import org.jetbrains.anko.toast
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by alexey on 23.02.16.
 */
class ReadActivity : AppCompatActivity() {
  @Inject lateinit var cache: ArticlesDAO
  val articleIds: MutableList<Int> = mutableListOf()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_read)
    setSupportActionBar(toolbar)
    getAppComponent().plus(ActivityModule(this)).inject(this)
    cache.getReadyArticlesIds()
      .subscribe {
        articleIds.addAll(it)
        loadArticle(it.get(0))
      }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.read, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item?.itemId) {
      R.id.read -> {
        cache.markArticleRead(articleIds.get(0))
          .subscribeOn(Schedulers.computation())
          .subscribe()
        articleIds.removeAt(0)
        if (articleIds.size > 0) {
          loadArticle(articleIds.get(0))
        } else {
          toast("No more articles")
          finish()
        }
      }
    }
    return true
  }

  fun loadArticle(id: Int) {
    cache.getArticle(id)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe {
        webView.loadData(prepareText(it.text), "text/html; charset=utf-8", "utf-8")
      }
  }

  fun prepareText(text: String): String {
    return "<!doctype html><html><head><meta charset=\"utf-8\"></head><body>$text</body>"
  }
}