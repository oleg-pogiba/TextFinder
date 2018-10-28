package com.marlin.webpagefinder.model

data class Page(val url: String) {
    var content: String = ""
    var status: Status = Status.NEW
    var statusDescription: String = ""
    var matches: Int = 0

    enum class Status {
        NEW,
        LOADING,
        FOUND,
        NOT_FOUND,
        ERROR
    }
}