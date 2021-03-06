package ru.home.customvk.data.api

import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.home.customvk.utils.PreferencesUtils
import javax.inject.Inject

class ApiFactory @Inject constructor(preferencesUtils: PreferencesUtils) {

    private companion object {
        private const val API_ENDPOINT = "https://api.vk.com/method/"
        private const val API_VERSION_KEY_NAME = "v"
        private const val API_VERSION_VALUE = "5.124"
        private const val ACCESS_TOKEN_KEY_NAME = "access_token"
    }

    val postApi: PostApi by lazy { buildRetrofit().create(PostApi::class.java) }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    private val requestInterceptor: Interceptor by lazy {
        Interceptor { chain ->
            val url: HttpUrl = chain.request().url.newBuilder()
                .addQueryParameter(ACCESS_TOKEN_KEY_NAME, preferencesUtils.accessToken)
                .addQueryParameter(API_VERSION_KEY_NAME, API_VERSION_VALUE)
                .build()

            chain.proceed(
                chain.request().newBuilder().url(url).build()
            )
        }
    }

    private fun buildRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_ENDPOINT)
            .client(buildClient())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    private fun buildClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(requestInterceptor)
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor(StethoInterceptor())
            .build()
    }
}
