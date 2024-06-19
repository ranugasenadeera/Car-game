package com.example.exam03

import GameView
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.exam03.R.*

class Score : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        // home button click listener
        var homeBtn = findViewById<Button>(R.id.homebtn)
        homeBtn.setOnClickListener {
           val intent = Intent(this, Home::class.java)
            startActivity(intent)
         }
    }
}