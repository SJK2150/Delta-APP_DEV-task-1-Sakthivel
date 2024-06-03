package com.example.deltaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name)

        val editText1: EditText = findViewById(R.id.playeroneenter)
        val editText2: EditText = findViewById(R.id.playertwoenter)
        val button: Button = findViewById(R.id.playButton)

        val gridfive = findViewById<Button>(R.id.gridfive)
        val gridsix = findViewById<Button>(R.id.gridsix)
        val gridseven = findViewById<Button>(R.id.gridseven)

        // Set up button click listener
        button.setOnClickListener {
            // Get the input from EditText
            val playerOneInput = editText1.text.toString()
            val playerTwoInput = editText2.text.toString()

            // Do something with the input
            // For demonstration, we are showing a Toast message
            Toast.makeText(this, "Input: $playerOneInput, $playerTwoInput", Toast.LENGTH_SHORT).show()

            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("PLAYER_ONE_NAME", playerOneInput)
                putExtra("PLAYER_TWO_NAME", playerTwoInput)
                putExtra("TOURNAMENT_TYPE", "BEST_OF_ONE")
            }
            startActivity(intent)
        }

        // Set up grid button click listeners
        gridfive.setOnClickListener {
            startMainActivity("five", editText1, editText2)
        }

        gridsix.setOnClickListener {
            startMainActivity("six", editText1, editText2)
        }

        gridseven.setOnClickListener {
            startMainActivity("seven", editText1, editText2)
        }
    }

    private fun startMainActivity(gridSize: String, editText1: EditText, editText2: EditText) {
        val playerOneInput = editText1.text.toString()
        val playerTwoInput = editText2.text.toString()




        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("PLAYER_ONE_NAME", playerOneInput)
            putExtra("PLAYER_TWO_NAME", playerTwoInput)
            putExtra("grid_size", gridSize)
            putExtra("TOURNAMENT_TYPE", intent.getStringExtra("TOURNAMENT_TYPE") ?: "BEST_OF_ONE")
        }

        startActivity(intent)
    }
}
