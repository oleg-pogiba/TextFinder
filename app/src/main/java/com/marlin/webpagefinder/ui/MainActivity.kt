package com.marlin.webpagefinder.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.marlin.webpagefinder.R
import com.marlin.webpagefinder.model.Page
import com.marlin.webpagefinder.presenter.MainContract
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity(), MainContract.View {
    // Inject MainPresenter
    private val presenter: MainContract.Presenter  by inject()
    private val adapter: UrlAdapter = UrlAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
        init()
    }

    override fun onStop() {
        super.onStop()
        presenter.detachView()
    }

    override fun updateUI(pages: MutableSet<Page>, progress: Int) {
        adapter.update(pages.toList())
        progressBar.progress = progress
    }

    override fun showUrlInvalidError() {
        url.error = getString(R.string.error_invalid_url)
    }

    override fun showSearchQueryInvalidError() {
        searchQuery.error = getString(R.string.error_invalid_search_query)
    }

    override fun showThreadAmountInvalidError() {
        maxThreadAmount.error = getString(R.string.error_invalid_thread_amount)
    }

    override fun showLimitUrlsInvalidError() {
        maxUrlAmount.error = getString(R.string.error_invalid_limit)
    }

    private fun init() {
        // debug data
//        url.editText?.setText("https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/to-int-or-null.html")
//        searchQuery.editText?.setText("kotlin")
//        maxUrlAmount.editText?.setText("50")
//        maxThreadAmount.editText?.setText("4")

        initOnClickActions()

        initRecyclerView()

        onValidateData()

        setSupportActionBar(toolbar)
    }

    private fun initRecyclerView() {
        resultList.layoutManager = LinearLayoutManager(this)

        // Access the RecyclerView Adapter and load the data into it
        resultList.adapter = adapter
    }

    private fun initOnClickActions() {
        start.setOnClickListener { onStartSearching() }
        pause.setOnClickListener { onPauseSearching() }
        resume.setOnClickListener { onResumeSearching() }
        stop.setOnClickListener { onStopSearching() }
    }

    private fun onValidateData() {
        pause.isEnabled = false
        resume.isEnabled = false
        stop.isEnabled = false
    }

    private fun onStartSearching() {
        hideKeyboard()

        adapter.reset()
        clearErrors()

        pause.isEnabled = true
        resume.isEnabled = false
        stop.isEnabled = true

        val urlAmount = maxUrlAmount.editText?.text.toString().toIntOrNull() ?: -1
        val threadAmount = maxThreadAmount.editText?.text.toString().toIntOrNull() ?: -1
        val url = url.editText?.text.toString()
        val searchQuery = searchQuery.editText?.text.toString()

        presenter.startSearching(url, searchQuery, urlAmount, threadAmount)
    }

    private fun onPauseSearching() {
        pause.isEnabled = false
        resume.isEnabled = true
        stop.isEnabled = true

        presenter.pauseSearching()
    }

    private fun onResumeSearching() {
        pause.isEnabled = true
        resume.isEnabled = false

        presenter.resumeSearching()
    }

    private fun onStopSearching() {
        pause.isEnabled = false
        resume.isEnabled = false

        presenter.stopSearching()
    }

    private fun clearErrors() {
        maxUrlAmount.error = null
        maxThreadAmount.error = null
        url.error = null
        searchQuery.error = null
    }

    private fun Context.hideKeyboard() {
        val view: View = if (currentFocus == null) View(this) else currentFocus
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
