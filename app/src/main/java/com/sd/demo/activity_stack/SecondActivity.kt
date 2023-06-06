package com.sd.demo.activity_stack

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.sd.demo.activity_stack.databinding.ActivityMainBinding

class SecondActivity : ComponentActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btn.setOnClickListener {
            finish()
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