package com.mrsep.musicrecognizer.data.di

import android.content.Context
import android.os.Build
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.util.getAppVersion
import com.mrsep.musicrecognizer.data.BuildConfig
import com.mrsep.musicrecognizer.data.ConnectivityManagerNetworkMonitor
import com.mrsep.musicrecognizer.data.NetworkMonitorDo
import com.mrsep.musicrecognizer.data.util.HttpFileLoggingInterceptor
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext appContext: Context,
        @ApplicationScope appScope: CoroutineScope
    ): OkHttpClient {
        return OkHttpClient.Builder()
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

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal interface NetworkMonitorModule {

    @Binds
    fun bindNetworkMonitor(impl: ConnectivityManagerNetworkMonitor): NetworkMonitorDo

}

private class UserAgentInterceptor(appContext: Context) : Interceptor {

    private val userAgent =
        "Audile/${appContext.getAppVersion()} (Android ${Build.VERSION.RELEASE})"

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .build()
        )
    }

}
