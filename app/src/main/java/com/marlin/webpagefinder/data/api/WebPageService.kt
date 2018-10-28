package com.marlin.webpagefinder.data.api

import io.reactivex.Single
import okhttp3.HttpUrl
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface WebPageService {
    @GET
    fun getContent(@Url url: HttpUrl): Single<ResponseBody>
}