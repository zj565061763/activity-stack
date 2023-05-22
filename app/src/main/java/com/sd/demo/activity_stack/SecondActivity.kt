package com.sd.demo.activity_stack

import android.os.Bundle
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
import com.sd.demo.activity_stack.ui.theme.AppTheme

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Content(
                    onClickFinish = {
                        finish()
                    },
                )
            }
        }

        logActivity("onCreate", this)
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
}

@Composable
private fun Content(
    onClickFinish: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Button(
            onClick = onClickFinish
        ) {
            Text(text = "finish")
        }
    }
}