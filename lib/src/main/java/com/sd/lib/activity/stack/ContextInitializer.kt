package com.sd.lib.activity.stack

import android.content.Context
import androidx.startup.Initializer

internal class ContextInitializer : Initializer<Context> {
    override fun create(context: Context): Context {
        FActivityStack.init(context)
        return context
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}