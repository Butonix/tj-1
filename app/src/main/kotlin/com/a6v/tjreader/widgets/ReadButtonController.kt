package com.a6v.tjreader.widgets

import com.a6v.tjreader.activities.ReadActivity
import com.a6v.tjreader.extensions.startActivityWithClass
import com.a6v.tjreader.extensions.toggleVisibility
import com.a6v.tjreader.fragments.BaseFragment
import com.a6v.tjreader.service.ArticleCount
import com.a6v.tjreader.service.ArticlesService
import com.trello.rxlifecycle.FragmentEvent
import kotlinx.android.synthetic.main.widget_btn_read.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

object ReadButtonController {
  fun run(service: ArticlesService, fragment: BaseFragment) {
    val read = fragment.read
    read.setOnClickListener({
      fragment.activity.startActivityWithClass(ReadActivity::class.java)
    })
    fragment.lifecycle()
      .filter { FragmentEvent.RESUME.equals(it) }
      .flatMap {
        //TODO notify about changes
        service.observeArticleCount()
          .subscribeOn(Schedulers.computation())
          .observeOn(AndroidSchedulers.mainThread())
          .compose(fragment.bindUntilEvent<ArticleCount>(FragmentEvent.PAUSE))
      }
      .subscribe {
        read.toggleVisibility(it.total > 0)
        if (it.loaded != it.total) {
          read.text = "Read (${it.loaded} / ${it.total})"
        } else {
          read.text = "Read (${it.loaded})"
        }
        read.isEnabled = it.loaded > 0
      }
  }
}