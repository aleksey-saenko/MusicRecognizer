package com.mrsep.musicrecognizer.data.di

import android.content.Context
import android.widget.Toast
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.data.BuildConfig
import com.mrsep.musicrecognizer.data.preferences.PreferencesDataRepository
import com.mrsep.musicrecognizer.data.remote.audd.websocket.scarlet.AuddScarletApi
import com.mrsep.musicrecognizer.data.remote.audd.model.AuddResponseJson
import com.mrsep.musicrecognizer.data.remote.audd.model.adapter.AuddJsonAdapter
import com.mrsep.musicrecognizer.data.remote.audd.toAuddReturnParameter
import com.mrsep.musicrecognizer.data.remote.audd.websocket.scarlet.FlowStreamAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.OkHttpClientWebSocketConnectionEstablisher
import com.tinder.scarlet.websocket.okhttp.OkHttpWebSocket
import com.tinder.scarlet.websocket.okhttp.request.RequestFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis


private const val AUDD_REST_BASE_URL = "https://api.audd.io/"
private const val AUDD_WEB_SOCKET_URL = "wss://api.audd.io/ws/?return=%s&api_token=%s"

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
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext appContext: Context,
        @ApplicationScope appScope: CoroutineScope
    ): OkHttpClient {
        return OkHttpClient.Builder()
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


    @Provides
    @Singleton
    fun providesAuddScarletApi(
        @ApplicationContext appContext: Context,
        okHttpClient: OkHttpClient,
        moshi: Moshi,
        auddRequestFactory: AuddRequestFactory
    ): AuddScarletApi {
        val scarlet: Scarlet
        val api: AuddScarletApi
        measureTimeMillis {
            scarlet = Scarlet.Builder()
                .backoffStrategy(LinearBackoffStrategy(5_000))
//            .lifecycle(AndroidLifecycle.ofApplicationForeground(appContext as Application))
                .webSocketFactory(okHttpClient.auddWebSocketFactory(auddRequestFactory))
                .addStreamAdapterFactory(FlowStreamAdapter.Factory)
                .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
                .build()
        }.run { println("TIME Scarlet=$this") }
        measureTimeMillis {
            api = scarlet.create()
        }.run { println("TIME Scarlet Api=$this") }
        return api
    }


    private fun OkHttpClient.auddWebSocketFactory(requestFactory: RequestFactory): WebSocket.Factory {
        return OkHttpWebSocket.Factory(
            OkHttpClientWebSocketConnectionEstablisher(this, requestFactory)
        )
    }

}

class AuddRequestFactory @Inject constructor(
    private val preferencesRepository: PreferencesDataRepository
) : RequestFactory {

    override fun createRequest(): Request {

        val userPreferences = runBlocking {// main thread!
            preferencesRepository.userPreferencesFlow.first()
        }
        val token = userPreferences.apiToken
        val returnParam = userPreferences.requiredServices.toAuddReturnParameter()
        return Request.Builder()
            .url(AUDD_WEB_SOCKET_URL.format(returnParam, token))
            .build()
    }

}

class HttpFileLoggingInterceptor(
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
