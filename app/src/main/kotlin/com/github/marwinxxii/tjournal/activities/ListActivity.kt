package com.github.marwinxxii.tjournal.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.github.marwinxxii.tjournal.EventBus
import com.github.marwinxxii.tjournal.R
import com.github.marwinxxii.tjournal.entities.ArticlePreview
import com.github.marwinxxii.tjournal.service.ArticlesService
import com.github.marwinxxii.tjournal.extensions.getAppComponent
import com.github.marwinxxii.tjournal.extensions.startActivityWithClass
import com.github.marwinxxii.tjournal.widgets.ArticleViewHolder
import com.github.marwinxxii.tjournal.widgets.DownloadArticleEvent
import kotlinx.android.synthetic.main.activity_list.*
import org.jsoup.Jsoup
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by alexey on 20.02.16.
 */
class ListActivity : AppCompatActivity() {
  lateinit var component: ActivityComponent
  @Inject lateinit var service: ArticlesService
  @Inject lateinit var eventBus: EventBus

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_list)
    setSupportActionBar(toolbar)
    component = getAppComponent().plus(ActivityModule(this))
    component.inject(this)
    items.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    val adapter = ArticlesAdapter()
    items.adapter = adapter
    service.getArticles(0)
      .map { it.map {
        val intro = Jsoup.parse(it.intro).text()
        it.copy(intro = intro)
      } }
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe {
        adapter.items = it
        adapter.notifyDataSetChanged()
      }

    eventBus.observe(DownloadArticleEvent::class.java).subscribe {
      service.getArticle(it.article)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          Toast.makeText(this, it.preview.title, Toast.LENGTH_SHORT).show()
        }
    }

    service.observeArticleCount()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe {
        if (it.total > 0) {
          read.visibility = View.VISIBLE
          read.text = "Read ${it.loaded} / ${it.total}"
        } else {
          read.visibility = View.GONE
        }
      }

    read.setOnClickListener({
      startActivityWithClass(ReadActivity::class.java)
    })
  }

  inner class ArticlesAdapter : RecyclerView.Adapter<ArticleViewHolder>() {
    val inflater: LayoutInflater = LayoutInflater.from(this@ListActivity)
    var items: List<ArticlePreview> = emptyList()

    override fun getItemCount(): Int {
      return items.size
    }

    override fun onBindViewHolder(viewHolder: ArticleViewHolder, position: Int) {
      viewHolder.bind(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ArticleViewHolder {
      val view = inflater.inflate(R.layout.widget_article_preview, parent, false)
      return ArticleViewHolder(view, eventBus)
    }
  }
}