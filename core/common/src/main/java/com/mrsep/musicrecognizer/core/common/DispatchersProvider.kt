package com.mrsep.musicrecognizer.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

interface DispatchersProvider {
    val main: CoroutineDispatcher
    val mainImmediate: CoroutineDispatcher
    val default: CoroutineDispatcher
    val io: CoroutineDispatcher
}

class DefaultDispatchersProvider @Inject constructor() : DispatchersProvider {
    override val main = Dispatchers.Main
    override val mainImmediate = Dispatchers.Main.immediate
    override val default = Dispatchers.Default
    override val io = Dispatchers.IO
}

class TestDispatchersProvider(testDispatcher: CoroutineDispatcher) : DispatchersProvider {
    override val main = testDispatcher
    override val mainImmediate = testDispatcher
    override val default = testDispatcher
    override val io = testDispatcher
}