package com.marlin.webpagefinder.presenter

import com.marlin.webpagefinder.model.Page

interface MainContract {
    interface Presenter {
        fun attachView(view: View)
        fun detachView()

        fun startSearching(baseUrl: String, searchQuery: String, limit: Int, threadAmount: Int)
        fun stopSearching()
        fun pauseSearching()
        fun resumeSearching()
    }

    interface View {
        fun updateUI(pages: MutableSet<Page>, progress: Int)
        fun showUrlInvalidError()
        fun showSearchQueryInvalidError()
        fun showThreadAmountInvalidError()
        fun showLimitUrlsInvalidError()
    }
}
