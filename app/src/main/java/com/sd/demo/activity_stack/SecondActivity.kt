package com.sd.demo.activity_stack

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn -> finish()
        }
    }
}