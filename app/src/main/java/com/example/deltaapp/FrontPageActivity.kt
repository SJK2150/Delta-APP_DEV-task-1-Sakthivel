package com.example.deltaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class FrontPageActivity : AppCompatActivity() {

    private lateinit var redWinsTextView: TextView
    private lateinit var blueWinsTextView: TextView
    private var countRedWins: Int = 0
    private var countBlueWins: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_front)

        // Initialize TextViews


        // Initialize buttons
        val playButton = findViewById<Button>(R.id.playButton)
        val bestOfThreeButton = findViewById<Button>(R.id.bestofthreeButton)
        val bestOfFiveButton = findViewById<Button>(R.id.bestoffiveutton)





        // Set button click listeners
        playButton.setOnClickListener {
            startGameWithTournamentType("BEST_OF_ONE")
        }
        bestOfThreeButton.setOnClickListener {
            startGameWithTournamentType("BEST_OF_THREE")
        }
        bestOfFiveButton.setOnClickListener {
            startGameWithTournamentType("BEST_OF_FIVE")
        }
    }

    // Function to update the win counts in TextViews


    // Function to start game with the specified tournament type
    private fun startGameWithTournamentType(tournamentType: String) {
        val intent = Intent(this, NameActivity::class.java)
        intent.putExtra("TOURNAMENT_TYPE", tournamentType)

        startActivity(intent)
    }
}
