package com.sd.demo.activity_stack

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.sd.demo.activity_stack.ui.theme.AppTheme
import com.sd.lib.actstack.FActivityStack
import com.sd.lib.actstack.fLastActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val _scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Content(
                    onClick = {
                        startActivity(Intent(this, SecondActivity::class.java))
                    },
                )
            }
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
        logActivity("onDestroy", this)
    }

    companion object {
        init {
            FActivityStack.isDebug = true
        }
    }
}

@Composable
private fun Content(
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Button(
            onClick = onClick
        ) {
            Text(text = "Go SecondActivity")
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
    val last = fLastActivity()
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