package com.mrsep.musicrecognizer.core.network

import android.content.Context
import android.os.Build
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.util.getAppVersionName
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext appContext: Context,
        @ApplicationScope appScope: CoroutineScope
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .pingInterval(15, TimeUnit.SECONDS)
            .addInterceptor(UserAgentInterceptor(appContext))
            .run {
                if ((BuildConfig.LOG_DEBUG_MODE)) {
                    val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    }
                    val httpFileLoggingInterceptor =
                        HttpFileLoggingInterceptor(appContext, appScope)
                    this.addInterceptor(httpLoggingInterceptor)
                        .addInterceptor(httpFileLoggingInterceptor)
                } else {
                    this
                }
            }
            .build()
    }
}

private class UserAgentInterceptor(appContext: Context) : Interceptor {

    private val userAgent =
        "Audile/${appContext.getAppVersionName()} (Android ${Build.VERSION.RELEASE})"

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .build()
        )
    }
}
