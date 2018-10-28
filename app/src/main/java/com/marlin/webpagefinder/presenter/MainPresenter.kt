package com.marlin.webpagefinder.presenter

import com.marlin.webpagefinder.interactor.WebPageInteractor
import com.marlin.webpagefinder.model.Page
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.Executors
import io.reactivex.subjects.PublishSubject


class MainPresenter(private val interactor: WebPageInteractor) : MainContract.Presenter {

    private lateinit var subscription: Disposable
    private var limit: Int = 0
    private var pages = mutableSetOf<Page>()
    private var view: MainContract.View? = null
    private val statusObserver: PublishSubject<Page> = PublishSubject.create<Page>()

    override fun attachView(view: MainContract.View) {
        this.view = view

        statusObserver
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { onChanged() }
    }

    override fun detachView() {
        this.view = null
    }

    override fun startSearching(baseUrl: String, searchQuery: String, limit: Int, threadAmount: Int) {
        if (isInputDataValid(baseUrl, searchQuery, limit, threadAmount)) {
            this.pages.clear()

            this.limit = limit
            interactor.init(searchQuery, getBackgroundScheduler(threadAmount), statusObserver)
            addPages(mutableSetOf(Page(baseUrl)))

            this.process()
        }
    }

    override fun resumeSearching() {
        interactor.resume()
        Timber.d("resume search")
        process()
    }

    override fun stopSearching() {
        subscription.dispose()
        Timber.d("stop searching")
    }

    override fun pauseSearching() {
        interactor.pause()
        Timber.d("pause searching")
    }


    private fun onChanged() {
        view?.updateUI(pages, defineProgress())
    }

    private fun defineProgress(): Int {
        val processedState = listOf(Page.Status.FOUND, Page.Status.NOT_FOUND, Page.Status.ERROR)
        val processed = pages.asSequence()
                .filter { it.status in processedState }
                .toList().size

        val progress = (processed.toFloat() / this.limit.toFloat() * 100).toInt()

        Timber.d("current progress: %s", progress)

        return progress
    }

    private fun isInputDataValid(baseUrl: String, searchQuery: String, limit: Int, threadAmount: Int): Boolean {
        var validationError = true

        val urlPattern = "(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
        val urlRegex = urlPattern.toRegex()

        if (!urlRegex.containsMatchIn(baseUrl)) {
            view?.showUrlInvalidError()
            validationError = false
        }

        if (searchQuery.length !in 1..100) {
            view?.showSearchQueryInvalidError()
            validationError = false
        }

        if (limit !in 1..1000) {
            view?.showLimitUrlsInvalidError()
            validationError = false
        }

        if (threadAmount !in 1..20) {
            view?.showThreadAmountInvalidError()
            validationError = false
        }

        return validationError
    }

    private fun process() {
        val readyToProcessing = pages.asSequence()
                .filter { it.status == Page.Status.NEW }
                .toList()

        if (readyToProcessing.isNotEmpty()) {
            interactor.process(readyToProcessing)
                    .doOnSubscribe { subscription = it }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                onProcessed(it)
                            },
                            { error ->

                            }
                    )
        }
    }

    private fun onProcessed(urls: MutableSet<String>) {
        if (pages.size == this.limit) {
            onChanged()
            return
        }

        val readyToProcess = mutableSetOf<Page>()

        urls.asSequence()
                .map { Page(it) }
                .filter { it !in pages }
                .filter { (pages.size + readyToProcess.size) < this.limit }
                .map { readyToProcess.add(it) }
                .toSet()

        Timber.d("added %s new pages: %s", readyToProcess.size, readyToProcess)

        addPages(readyToProcess)
        process()
    }

    private fun addPages(readyToProcess: MutableSet<Page>) {
        this.pages.addAll(readyToProcess)
        onChanged()
    }

    private fun getBackgroundScheduler(threadAmount: Int): Scheduler {
        val threadPool = Executors.newFixedThreadPool(threadAmount)
        return Schedulers.from(threadPool)
    }
}
