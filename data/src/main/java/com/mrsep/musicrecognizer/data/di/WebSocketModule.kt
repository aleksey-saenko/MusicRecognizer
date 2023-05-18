package com.mrsep.musicrecognizer.data.di

import com.mrsep.musicrecognizer.data.remote.audd.websocket.AuddRecognitionWebSocketService
import com.mrsep.musicrecognizer.data.remote.audd.websocket.AuddRecognitionWebSocketServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface WebSocketModule {

    @Binds
    @Singleton
    fun bindAuddRecognitionWebSocketService(implementation: AuddRecognitionWebSocketServiceImpl):
            AuddRecognitionWebSocketService

}