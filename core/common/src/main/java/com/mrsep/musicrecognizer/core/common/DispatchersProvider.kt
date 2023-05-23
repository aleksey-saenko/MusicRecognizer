package com.mrsep.musicrecognizer.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatchersProvider {
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
    val io: CoroutineDispatcher
}

class DefaultDispatchersProvider : DispatchersProvider {
    override val main = Dispatchers.Main
    override val default = Dispatchers.Default
    override val io = Dispatchers.IO
}

class TestDispatchersProvider(testDispatcher: CoroutineDispatcher) : DispatchersProvider {
    override val main = testDispatcher
    override val default = testDispatcher
    override val io = testDispatcher
}