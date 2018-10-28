package com.marlin.webpagefinder.di

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.marlin.webpagefinder.data.api.WebPageService
import com.marlin.webpagefinder.interactor.WebPageInteractor
import com.marlin.webpagefinder.interactor.WebPageInteractorImpl
import com.marlin.webpagefinder.presenter.MainContract
import com.marlin.webpagefinder.presenter.MainPresenter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module.Module
import org.koin.dsl.module.applicationContext
import retrofit2.Retrofit

// Koin module
val repositoryModule: Module = applicationContext {
    bean { WebPageInteractorImpl(get()) as WebPageInteractor }
}

val presenterModule: Module = applicationContext {
    factory { MainPresenter(get()) as MainContract.Presenter }
}

val apiModule: Module = applicationContext {
    bean { provideOkHttpClient() }
    bean { provideWebPageService(get()) }
}


fun provideOkHttpClient(): OkHttpClient {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY
    return OkHttpClient.Builder().addInterceptor(interceptor).build()
}

fun provideWebPageService(client: OkHttpClient): WebPageService {
    val retrofit = Retrofit.Builder()
            .baseUrl("https://google.com")
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    return retrofit.create(WebPageService::class.java)
}