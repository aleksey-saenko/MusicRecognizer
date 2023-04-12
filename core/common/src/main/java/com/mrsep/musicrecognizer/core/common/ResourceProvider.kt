package com.mrsep.musicrecognizer.core.common

import android.content.Context

class ResourceProviderImpl(
    private val context: Context
): ResourceProvider {

    override fun getString(resId: Int) = context.getString(resId)
    override fun getString(resId: Int, vararg formatArgs: Any) = context.getString(resId, formatArgs)

}

interface ResourceProvider {
    fun getString(resId: Int, vararg formatArgs: Any): String
    fun getString(resId: Int): String
}