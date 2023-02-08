package com.mrsep.musicrecognizer.domain

import java.io.File

interface PlayerController {

//    val statusFlow: Flow<PlayerStatus>

    fun startPlay(file: File)
    fun stopPlay()

}

sealed class PlayerStatus {
    object Ready : PlayerStatus()
    object Playing : PlayerStatus()
    object Stopped : PlayerStatus()
    object Failure : PlayerStatus()
}