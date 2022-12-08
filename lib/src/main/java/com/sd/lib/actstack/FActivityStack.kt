package com.sd.lib.actstack

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.util.Log
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

object FActivityStack {
    private val _objectInitFlag = AtomicBoolean(false)

    private val _activityDistinct: MutableMap<Activity, String> = WeakHashMap()
    private val _activityHolder: MutableList<Activity> = CopyOnWriteArrayList()

    var isDebug = false

    internal fun init(context: Context) {
        if (_objectInitFlag.compareAndSet(false, true)) {
            val application = context.applicationContext as Application
            application.registerActivityLifecycleCallbacks(_activityLifecycleCallbacks)
        }
    }

    @JvmStatic
    fun last(): Activity? {
        synchronized(FActivityStack) {
            while (true) {
                val activity = _activityHolder.lastOrNull() ?: break
                if (activity.isFinishing) {
                    removeActivity(activity)
                } else {
                    return activity
                }
            }
            return null
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
            if (activity.isFinishing) {
                if (_activityHolder.remove(activity)) {
                    logMsg {
                        """
                    ----- $activity ${_activityHolder.size}
                    ${_activityHolder.joinToString(prefix = "[", separator = ", ", postfix = "]")}
                """.trimIndent()
                    }
                }
            }
        }
    }

    private fun removeFinishingActivity() {
        last()
    }

    private val _activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {

        // ---------- pre ----------

        override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
            addActivity(activity)
        }

        override fun onActivityPreStarted(activity: Activity) {
            removeFinishingActivity()
        }

        override fun onActivityPreResumed(activity: Activity) {
            removeFinishingActivity()
        }

        override fun onActivityPreDestroyed(activity: Activity) {
            removeActivity(activity)
        }

        // ---------- default ----------

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            addActivity(activity)
        }

        override fun onActivityStarted(activity: Activity) {
            removeFinishingActivity()
        }

        override fun onActivityResumed(activity: Activity) {
            removeFinishingActivity()
        }

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            removeActivity(activity)
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
        synchronized(FActivityStack) {
            _activityHolder.forEach {
                if (it.javaClass == clazz) {
                    return it
                }
            }
            return null
        }
    }

    @JvmStatic
    fun finishAll() {
        synchronized(FActivityStack) {
            _activityHolder.forEach {
                it.finish()
            }
        }
    }

    @JvmStatic
    fun finishAllExpect(activity: Activity) {
        synchronized(FActivityStack) {
            _activityHolder.forEach {
                if (it !== activity) {
                    it.finish()
                }
            }
        }
    }

    @JvmStatic
    fun finishAllExpect(vararg classes: Class<out Activity>) {
        synchronized(FActivityStack) {
            if (classes.isEmpty()) return
            _activityHolder.forEach {
                if (it.javaClass !in classes) {
                    it.finish()
                }
            }
        }
    }

    @JvmStatic
    fun finish(vararg classes: Class<out Activity>) {
        synchronized(FActivityStack) {
            if (classes.isEmpty()) return
            _activityHolder.forEach {
                if (it.javaClass in classes) {
                    it.finish()
                }
            }
        }
    }

    @JvmStatic
    fun snapshot(block: (List<Activity>) -> Unit) {
        synchronized(FActivityStack) {
            block(_activityHolder.toList())
        }
    }

    // ---------- await ----------

    private val _awaitHolder: MutableMap<Class<out Activity>, FContinuation<Activity>> by lazy { hashMapOf() }

    suspend fun await(clazz: Class<out Activity>): Activity {
        val continuation = synchronized(FActivityStack) {
            val activity = getFirst(clazz)
            if (activity != null) return activity

            _awaitHolder[clazz] ?: FContinuation<Activity>().also {
                _awaitHolder[clazz] = it
            }
        }
        return continuation.await()
    }

    private fun resumeAwait(activity: Activity) {
        synchronized(FActivityStack) {
            val continuation = _awaitHolder.remove(activity.javaClass) ?: return
            continuation.resume(activity)
        }
    }
}

fun fLastActivity(): Activity? = FActivityStack.last()

internal inline fun logMsg(block: () -> String) {
    if (FActivityStack.isDebug) {
        Log.i("activity-stack", block())
    }
}