package com.sd.demo.activity_stack

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.sd.lib.actstack.FActivityStack
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val _scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logActivity("onCreate", this)

        _scope.launch {
            logMsg { "await SecondActivity start" }
            FActivityStack.await(SecondActivity::class.java).let {
                logMsg { "await SecondActivity end $it ${it.activityState}" }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        logActivity("onStart", this)
    }

    override fun onResume() {
        super.onResume()
        logActivity("onResume", this)
    }

    override fun onPause() {
        super.onPause()
        logActivity("onPause", this)
    }

    override fun onStop() {
        super.onStop()
        logActivity("onStop", this)
    }

    override fun onDestroy() {
        super.onDestroy()
        logActivity("onDestroy", this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn -> startActivity(Intent(this, SecondActivity::class.java))
        }
    }

    companion object {
        init {
            FActivityStack.isDebug = true
        }
    }
}

val Activity.activityState: Lifecycle.State?
    get() {
        return if (this is LifecycleOwner) {
            lifecycle.currentState
        } else {
            null
        }
    }

fun logActivity(tag: String, activity: AppCompatActivity) {
    val last = FActivityStack.last()
    logMsg {
        """
             
            $tag $activity ${activity.activityState}
            last:$last ${last?.activityState}
             
        """.trimIndent()
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("activity-stack-demo", block())
}