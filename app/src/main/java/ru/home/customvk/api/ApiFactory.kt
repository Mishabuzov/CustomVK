package ru.home.customvk.api

import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiFactory {

    private const val API_ENDPOINT = "https://api.vk.com/method/"
    private const val API_VERSION_KEY_NAME = "v"
    private const val API_VERSION_VALUE = "5.124"
    private const val ACCESS_TOKEN_KEY_NAME = "access_token"

    lateinit var accessToken: String

    val postsApi: PostsApi by lazy { buildRetrofit().create(PostsApi::class.java) }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    private val requestInterceptor: Interceptor by lazy {
        Interceptor { chain ->
            val url: HttpUrl = chain.request().url.newBuilder()
                .addQueryParameter(ACCESS_TOKEN_KEY_NAME, accessToken)
                .addQueryParameter(API_VERSION_KEY_NAME, API_VERSION_VALUE)
                .build()

            chain.proceed(
                chain.request().newBuilder().url(url).build()
            )
        }
    }

    private fun buildRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(API_ENDPOINT)
        .client(buildClient())
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    private fun buildClient() = OkHttpClient.Builder()
        .addInterceptor(requestInterceptor)
        .addInterceptor(loggingInterceptor)
        .addNetworkInterceptor(StethoInterceptor())
        .build()

}
