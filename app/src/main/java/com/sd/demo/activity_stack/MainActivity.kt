package com.sd.demo.activity_stack

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.sd.demo.activity_stack.databinding.ActivityMainBinding
import com.sd.lib.activity.stack.FActivityStack
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val _scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btn.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }

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
        _scope.cancel()
        logActivity("onDestroy", this)
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

fun logActivity(tag: String, activity: ComponentActivity) {
    logMsg { "$tag $activity ${activity.activityState}" }
}

inline fun logMsg(block: () -> String) {
    Log.i("activity-stack-demo", block())
}