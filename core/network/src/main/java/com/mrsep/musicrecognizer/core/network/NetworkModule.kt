package com.mrsep.musicrecognizer.core.network

import android.content.Context
import android.os.Build
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.util.getAppVersionName
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

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
    fun provideKtorClient(
        @ApplicationContext appContext: Context,
        @ApplicationScope appScope: CoroutineScope,
        json: Json
    ): HttpClient = HttpClient(OkHttp) {
        engine {
            config {
                pingInterval(15.seconds)
            }
        }
        install(ContentNegotiation) {
            json(json)
            // ACRCloud returns json as text/plain
            register(ContentType.Text.Plain, KotlinxSerializationConverter(json))
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 20_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 10_000
        }
        install(WebSockets) {
            // Use the engine configuration to set the ping interval for OkHttp (ktor docs)
            pingInterval = 15.seconds
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }
        install(UserAgentIfMissing) {
            agent = "Audile/${appContext.getAppVersionName(removeDebug = true)}" +
                    " (Android ${Build.VERSION.RELEASE})"
        }
        if (BuildConfig.DEBUG) {
            install(Logging) {
                logger = object : Logger {
                    val logcatLogger = Logger.ANDROID
                    val fileLogger = HttpFileLogger(appContext, appScope)
                    override fun log(message: String) {
                        logcatLogger.log(message)
                        fileLogger.log(message)
                    }
                }
                level = LogLevel.ALL
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
        }
    }

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext appContext: Context,
        httpClient: dagger.Lazy<HttpClient>,
    ) = ImageLoader.Builder(appContext)
        .components {
            add(KtorNetworkFetcherFactory(httpClient = { httpClient.get() }))
        }
        .diskCache {
            DiskCache.Builder()
                .directory(appContext.cacheDir.resolve("image_cache"))
                .maxSizeBytes(512L * 1024 * 1024)
                .build()
        }
        .crossfade(true)
        .build()
}
