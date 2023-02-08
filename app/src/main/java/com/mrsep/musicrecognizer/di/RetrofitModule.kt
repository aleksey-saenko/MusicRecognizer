package com.mrsep.musicrecognizer.di

import android.content.Context
import android.widget.Toast
import com.mrsep.musicrecognizer.data.remote.audd.model.adapter.AuddErrorJsonAdapter
import com.mrsep.musicrecognizer.data.remote.audd.model.adapter.AuddSuccessJsonAdapter
import com.mrsep.musicrecognizer.data.remote.audd.model.AuddResponseJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
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
import javax.inject.Inject
import javax.inject.Singleton

private const val BASE_URL = "https://api.audd.io/"

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(
                PolymorphicJsonAdapterFactory.of(AuddResponseJson::class.java, "status")
                    .withSubtype(AuddResponseJson.Success::class.java, "success")
                    .withSubtype(AuddResponseJson.Error::class.java, "error")
            )
            .add(AuddSuccessJsonAdapter())
            .add(AuddErrorJsonAdapter())
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        moshi: Moshi,
        httpFileLoggingInterceptor: HttpFileLoggingInterceptor
    ): Retrofit {
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(httpFileLoggingInterceptor)
            .build()

        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BASE_URL)
            .build()
    }

}

@Singleton
class HttpFileLoggingInterceptor @Inject constructor(
    @ApplicationContext private val appContext: Context
) : Interceptor {
    @Suppress("SpellCheckingInspection")
    private val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
    private val rootDir = "${appContext.filesDir.absolutePath}/http_logger/"

    init {
        try {
            val root = File("${appContext.filesDir.absolutePath}/http_logger/")
            if (!root.exists()) {
                root.mkdir()
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val responseTime = sdf.format(Date(System.currentTimeMillis()))
        val responseFile = File("$rootDir/${responseTime}_response.txt")

        val body = response.body!!
        val json = body.string()
        writeLogFile(responseFile, JSONObject(json).toString(4))

        return response.newBuilder()
            .body(json.toResponseBody(body.contentType()))
            .build()
    }

    private fun writeLogFile(logFile: File, record: String) {
        try {
            logFile.outputStream().bufferedWriter().use { writer ->
                writer.write(record)
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun handleError(e: Exception) {
        e.printStackTrace()
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            Toast.makeText(
                appContext,
                "LogToStorage failed, check stacktrace",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}
