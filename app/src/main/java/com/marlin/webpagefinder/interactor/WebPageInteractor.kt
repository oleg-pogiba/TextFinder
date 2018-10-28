package com.marlin.webpagefinder.interactor

import com.marlin.webpagefinder.model.Page
import com.marlin.webpagefinder.presenter.MainContract
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import okhttp3.HttpUrl

interface WebPageInteractor {
    fun init(searchQuery: String, subscribeOnScheduler: Scheduler, statusObserver: PublishSubject<Page>)
    fun process(pages: List<Page>): Single<MutableSet<String>>
    fun pause()
    fun resume()
}