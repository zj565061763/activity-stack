package com.sd.lib.activity.stack

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.util.Log
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicBoolean

object FActivityStack {
    private val _initFlag = AtomicBoolean(false)

    private val _activityDistinct: MutableMap<Activity, String> = WeakHashMap()
    private val _activityHolder: MutableList<Activity> = mutableListOf()

    var isDebug = false

    internal fun init(context: Context) {
        if (_initFlag.compareAndSet(false, true)) {
            val application = context.applicationContext as Application
            application.registerActivityLifecycleCallbacks(_activityLifecycleCallbacks)
        }
    }

    private val _activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            addActivity(activity)
        }

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {
            if (activity.isFinishing) {
                removeActivity(activity)
            }
        }

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            removeActivity(activity)
        }
    }

    private fun addActivity(activity: Activity) {
        synchronized(FActivityStack) {
            if (activity.isFinishing) return
            if (_activityDistinct.containsKey(activity)) return

            _activityDistinct[activity] = ""
            _activityHolder.add(activity)
            resumeAwait(activity)

            logMsg {
                """
                    +++++ $activity ${_activityHolder.size}
                    ${_activityHolder.joinToString(prefix = "[", separator = ", ", postfix = "]")}
                """.trimIndent()
            }
        }
    }

    private fun removeActivity(activity: Activity) {
        synchronized(FActivityStack) {
            if (_activityHolder.remove(activity)) {
                _activityDistinct.remove(activity)
                logMsg {
                    """
                    ----- $activity ${_activityHolder.size}
                    ${_activityHolder.joinToString(prefix = "[", separator = ", ", postfix = "]")}
                """.trimIndent()
                }
            }
        }
    }

    // ---------- ext ----------

    @JvmStatic
    fun size(): Int {
        synchronized(FActivityStack) {
            return _activityHolder.size
        }
    }

    @JvmStatic
    fun getFirst(clazz: Class<out Activity>): Activity? {
        return snapshot { holder ->
            holder.find {
                it.javaClass == clazz
            }
        }
    }

    @JvmStatic
    fun finishAll() {
        snapshot { holder ->
            holder.forEach {
                it.finish()
            }
        }
    }

    @JvmStatic
    fun finishAllExpect(activity: Activity) {
        snapshot { holder ->
            holder.forEach {
                if (it !== activity) {
                    it.finish()
                }
            }
        }
    }

    @JvmStatic
    fun finishAllExpect(vararg classes: Class<out Activity>) {
        snapshot { holder ->
            holder.forEach {
                if (it.javaClass !in classes) {
                    it.finish()
                }
            }
        }
    }

    @JvmStatic
    fun finish(vararg classes: Class<out Activity>) {
        if (classes.isEmpty()) return
        snapshot { holder ->
            holder.forEach {
                if (it.javaClass in classes) {
                    it.finish()
                }
            }
        }
    }

    @JvmStatic
    fun <T> snapshot(block: (List<Activity>) -> T): T {
        synchronized(FActivityStack) {
            return block(_activityHolder.toList())
        }
    }

    // ---------- await ----------

    private val _awaitHolder: MutableMap<Class<out Activity>, FContinuation<Activity>> by lazy { hashMapOf() }

    suspend fun await(clazz: Class<out Activity>): Activity {
        return synchronized(FActivityStack) {
            val activity = getFirst(clazz)
            if (activity != null) return activity

            _awaitHolder[clazz] ?: FContinuation<Activity>().also {
                _awaitHolder[clazz] = it
            }
        }.let { cont ->
            cont.await(
                onCancel = {
                    if (cont.size() <= 0) {
                        synchronized(FActivityStack) {
                            _awaitHolder.remove(clazz)
                        }
                    }
                }
            )
        }
    }

    private fun resumeAwait(activity: Activity) {
        synchronized(FActivityStack) {
            _awaitHolder.remove(activity.javaClass)
        }?.resume(activity)
    }
}

internal inline fun logMsg(block: () -> String) {
    if (FActivityStack.isDebug) {
        Log.i("activity-stack", block())
    }
}