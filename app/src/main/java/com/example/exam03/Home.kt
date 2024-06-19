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

class Home : AppCompatActivity(), GameTask {
    //Declare layout elements and game related variables
    lateinit var mainlayout : LinearLayout
    lateinit var startBtn : Button
    lateinit var homeBtn : Button
    lateinit var mGameView : GameView
    lateinit var score: TextView
    private val HIGH_SCORE_KEY = "high_score"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)//Set activity layout from xml

        //Initialize layout elements by finding views from xml layout
        homeBtn = findViewById(R.id.homebtn)
        homeBtn.visibility = View.GONE
        startBtn = findViewById(R.id.startbtn)
        mainlayout = findViewById(R.id.mainlayout)
        score = findViewById(R.id.score)
        mGameView = GameView(this, this)//Initialize game view instance

        //Set click listener for start button to begin game
        startBtn.setOnClickListener{
            //Reset game state and prepare game view
            mGameView.resetGame()
            mGameView.setBackgroundResource(R.drawable.road)
            mainlayout.addView(mGameView)//Add game view to main layout
            startBtn.visibility = View.GONE//Hide start button
            score.visibility = View.GONE//Hide score text
            homeBtn.visibility = View.GONE//Hide home button

            val logo = findViewById<ImageView>(R.id.logo)//Remove logo from layout
            mainlayout.removeView(logo)

            mainlayout.background = null
        }
    }

    override fun closeGame(myScore: Int) {
        //Remove game view from layout when game ends
        mainlayout.removeView(mGameView)
        //ake start button and score text visible
        startBtn.visibility = View.VISIBLE
        homeBtn.visibility = View.VISIBLE
        score.visibility = View.VISIBLE

        //Navigate to home page
        homeBtn.setOnClickListener {
            var intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        // Access the shared preferences to retrieve and update the high score
        val sharedPreferences = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val highScore = sharedPreferences.getInt(HIGH_SCORE_KEY, 0)

        // Update the high score if the current score is higher than the stored high score
        if (myScore > highScore) {
            sharedPreferences.edit().putInt(HIGH_SCORE_KEY, myScore).apply()
        }

        // Retrieve the updated high score from shared preferences
        val savedHighScore = sharedPreferences.getInt(HIGH_SCORE_KEY, 0)
        // Update the score text to display the current score and the high score
        score.text = "Score : $myScore\n\nHigh Score : $savedHighScore"

        // Additional cleanup after removing the game view
        mainlayout.removeView(mGameView)
        startBtn.visibility = View.VISIBLE
        score.visibility = View.VISIBLE

    }
}