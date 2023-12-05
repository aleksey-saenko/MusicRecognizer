package com.mrsep.musicrecognizer.data.di

import android.content.Context
import android.os.Build
import android.widget.Toast
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.util.getAppVersion
import com.mrsep.musicrecognizer.data.BuildConfig
import com.mrsep.musicrecognizer.data.ConnectivityManagerNetworkMonitor
import com.mrsep.musicrecognizer.data.NetworkMonitorDo
import com.mrsep.musicrecognizer.data.remote.audd.json.AuddResponseJson
import com.mrsep.musicrecognizer.data.remote.audd.json.adapter.AuddJsonAdapter
import com.mrsep.musicrecognizer.data.remote.enhancer.odesli.OdesliJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

private const val AUDD_REST_BASE_URL = "https://api.audd.io/"
//private const val AUDD_WEB_SOCKET_URL = "wss://api.audd.io/ws/?return=%s&api_token=%s"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(
                PolymorphicJsonAdapterFactory.of(AuddResponseJson::class.java, "status")
                    .withSubtype(AuddResponseJson.Success::class.java, "success")
                    .withSubtype(AuddResponseJson.Error::class.java, "error")
            )
            .add(AuddJsonAdapter())
            .add(OdesliJsonAdapter())
            .build()
    }

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

    @Provides
    @Singleton
    fun provideRetrofit(
        moshi: Moshi,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(AUDD_REST_BASE_URL)
            .build()
    }

}

private class UserAgentInterceptor(appContext: Context) : Interceptor {

    private val userAgent = "Audile/${appContext.getAppVersion()} (Android ${Build.VERSION.RELEASE})"

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .build()
        )
    }

}


@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface NetworkMonitorModule {

    @Binds
    fun bindNetworkMonitor(implementation: ConnectivityManagerNetworkMonitor): NetworkMonitorDo

}


private class HttpFileLoggingInterceptor(
    private val appContext: Context,
    private val scope: CoroutineScope
) : Interceptor {

    @Suppress("SpellCheckingInspection")
    private val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
    private val rootDir = "${appContext.filesDir.absolutePath}/http_logger/"

    init {
        try {
            File("${appContext.filesDir.absolutePath}/http_logger/").run { if (!exists()) mkdir() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val responseTime = sdf.format(Date(System.currentTimeMillis()))
        val responseFile = File("$rootDir/${responseTime}_response.txt")

        val body = response.body!!
        val json = body.string()
        if (json.isNotBlank()) {
            writeLogFile(responseFile, JSONObject(json).toString(4))
        }

        return response.newBuilder()
            .body(json.toResponseBody(body.contentType()))
            .build()
    }

    private fun writeLogFile(logFile: File, record: String) {
        scope.launch(Dispatchers.IO) {
            try {
                logFile.outputStream().bufferedWriter().use { writer ->
                    writer.write(record)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        appContext,
                        "LogToStorage failed, check stacktrace",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

}
