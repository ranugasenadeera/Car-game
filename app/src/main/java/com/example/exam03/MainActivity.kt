package com.example.exam03

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class MainActivity : AppCompatActivity() {
    private val SPLASH_DURATION: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start timer to transition to main content after splash duration
        Handler().postDelayed({
            // Make main content visible
            startActivity(Intent(this, Home::class.java))

        }, SPLASH_DURATION)

    }
}