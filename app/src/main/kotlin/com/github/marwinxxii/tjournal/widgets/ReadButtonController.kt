package com.github.marwinxxii.tjournal.widgets

import com.github.marwinxxii.tjournal.activities.ReadActivity
import com.github.marwinxxii.tjournal.extensions.startActivityWithClass
import com.github.marwinxxii.tjournal.extensions.toggleVisibility
import com.github.marwinxxii.tjournal.fragments.BaseFragment
import com.github.marwinxxii.tjournal.service.ArticleCount
import com.github.marwinxxii.tjournal.service.ArticlesService
import com.trello.rxlifecycle.FragmentEvent
import kotlinx.android.synthetic.main.widget_btn_read.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by alexey on 09.03.16.
 */
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