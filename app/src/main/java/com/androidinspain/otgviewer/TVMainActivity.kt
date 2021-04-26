package com.androidinspain.otgviewer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

class TVMainActivity : Activity() {
    private val TAG = TVMainActivity::class.java.name
    private val DEBUG = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (DEBUG) Log.d(TAG, "onCreate TVMainActivity")
        val intent = Intent(this, MainActivity::class.java) // Your list's Intent
        startActivity(intent)
    }
}