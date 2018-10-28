package com.marlin.webpagefinder.interactor

import com.marlin.webpagefinder.data.api.WebPageService
import com.marlin.webpagefinder.model.Page
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import okhttp3.HttpUrl
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class WebPageInteractorImpl(val webPageService: WebPageService) : WebPageInteractor {
    private var resumed = AtomicBoolean()

    private lateinit var searchQuery: String
    private lateinit var subscribeOnScheduler: Scheduler
    private lateinit var statusPublishSubject: PublishSubject<Page>

    override fun init(searchQuery: String, subscribeOnScheduler: Scheduler, statusObserver: PublishSubject<Page>) {
        this.searchQuery = searchQuery
        this.subscribeOnScheduler = subscribeOnScheduler
        this.statusPublishSubject = statusObserver

        resumed.set(true)
    }

    override fun process(pages: List<Page>): Single<MutableSet<String>> {

        return Observable.fromIterable(pages)
                .flatMap { page ->
                    Observable.just(page)
                            .takeWhile { resumed.get() }
                            .map {
                                Timber.d("takes url: %s", it.url)
                                return@map it
                            }
                            .subscribeOn(subscribeOnScheduler)
                            .map { download(it) }
                            .filter { it.status != Page.Status.ERROR }
                            .map { find(it) }
                            .map { parse(it) }

                }
                .collectInto(mutableSetOf()) { list, t2 ->
                    list.addAll(t2)
                }
    }

    override fun pause() {
        resumed.set(false)
    }

    override fun resume() {
        resumed.set(true)
    }

    //improve: move to worker
    private fun download(page: Page): Page {
        Timber.d("web page is downloading on thread: %s", Thread.currentThread())
        onLoading(page)

        val httpUrl = HttpUrl.parse(page.url)
        if (httpUrl != null) {
            webPageService.getContent(httpUrl)
                    .subscribe(
                            {
                                page.content = it.string()
                                Timber.d("web page is downloaded successfully, url: %s", page.url)
                            },
                            { error ->
                                onDownloadFailed(page, error)
                            }
                    )
        }
        return page
    }

    //improve: move to worker
    private fun parse(page: Page): Set<String> {
        val urlRegex = "(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
        val ignoreRegex = "([^\\s]+(\\.(?i)(jpg|png|gif|bmp|js|css|ico))$)"

        val urlPattern = urlRegex.toRegex()
        val ignorePattern = ignoreRegex.toRegex()

        return urlPattern.findAll(page.content)
                .filter { !ignorePattern.matches(it.value) }
                .map {
                    Timber.d("%s found at indexes: %s", it.value, it.range)
                    return@map it.value
                }
                .toSet()
    }

    //improve: move to worker
    private fun find(page: Page): Page {
        val pattern = this.searchQuery.toRegex()
        val matches = pattern.findAll(page.content).toList().size
        page.matches = matches

        Timber.d("%s matches: %s", page.url, page.matches)

        onSearchFinished(page)
        return page
    }

    private fun onLoading(page: Page) {
        page.status = Page.Status.LOADING

        statusPublishSubject.onNext(page)
    }

    private fun onSearchFinished(page: Page) {
        if (page.matches > 0) {
            page.status = Page.Status.FOUND
        } else {
            page.status = Page.Status.NOT_FOUND
        }

        statusPublishSubject.onNext(page)
    }

    private fun onDownloadFailed(page: Page, ex: Throwable) {
        Timber.e(ex, "download is failed")

        page.status = Page.Status.ERROR
        page.statusDescription = ex.message.toString()

        statusPublishSubject.onNext(page)
    }
}