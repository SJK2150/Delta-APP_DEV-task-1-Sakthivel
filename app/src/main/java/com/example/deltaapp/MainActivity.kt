package com.example.deltaapp
import android.util.Log
import android.media.MediaPlayer
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.content.Intent
import com.example.deltaapp.model.Tile
import android.widget.ToggleButton
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator




class MainActivity : AppCompatActivity() {

    private var gridsizevalue = 7 // Default value
    private lateinit var mediaPlayer: MediaPlayer
    private val gameGrid = Array(gridsizevalue) { Array(gridsizevalue) { Tile() } }
    private var isPlayer1Turn = true
    private lateinit var rootView: View
    private lateinit var player1PointsTextView: TextView
    private lateinit var player2PointsTextView: TextView
    private var moveCount = 0

    private lateinit var playerName1: String
    private lateinit var playerName2: String
    private var player1Wins = 0
    private var player2Wins = 0
    private var totalGames = 1
    private var maxGames = 1
    private var previousLoser: String? = null
    private var powerUpUsed = false // Flag to track if power-up has been used

    // Declare power-up buttons
    private lateinit var powerup1Button: ToggleButton
    private lateinit var powerup2Button: ToggleButton
    private var countbluewins=0
    private var countredwins=0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaPlayer = MediaPlayer.create(this, R.raw.powerupsound)
        // Retrieve grid size from intent extras
        val gridsize = intent.getStringExtra("grid_size")
        gridsizevalue = when (gridsize) {
            "five" -> 5
            "six" -> 6
            "seven" -> 7
            else -> 5
        }

        playerName1 = intent.getStringExtra("PLAYER_ONE_NAME") ?: "Player 1"
        playerName2 = intent.getStringExtra("PLAYER_TWO_NAME") ?: "Player 2"

        setContentView(R.layout.activity_main)
        rootView = findViewById(R.id.rootView)

        player1PointsTextView = findViewById(R.id.player1points)
        player2PointsTextView = findViewById(R.id.player2points)

        // Initialize power-up buttons
        powerup1Button = findViewById(R.id.powerup1Button)
        powerup2Button = findViewById(R.id.powerup2Button)

        // Set initial background color
        updateBackgroundColor()
        val tournamentType = intent.getStringExtra("TOURNAMENT_TYPE")
        // Set max games based on tournament type
        maxGames = when (tournamentType) {
            "BEST_OF_ONE" -> 1
            "BEST_OF_THREE" -> 3
            "BEST_OF_FIVE" -> 5
            else -> 3
        }

        val gridLayout = findViewById<GridLayout>(R.id.gameGrid)

        // Initialize grid after getting grid size from intent
        initializeGrid(gridLayout)

        findViewById<Button>(R.id.resetButton).setOnClickListener { resetGame() }
        findViewById<Button>(R.id.rulesButton).setOnClickListener { showGameRulesDialog() }

        // Show rules on startup
        showGameRulesDialog()
        val player1display= findViewById<TextView>(R.id.playeroneone)
        player1display.text=playerName1
        val player2display= findViewById<TextView>(R.id.playertwotwo)
        player2display.text=playerName2

    }



    private fun updateBackgroundColor() {
        val playerColor = if (isPlayer1Turn) {
            ContextCompat.getColor(this, R.color.light_red)
        } else {
            ContextCompat.getColor(this, R.color.light_blue)
        }
        rootView.setBackgroundColor(playerColor)
        updateGrid(playerColor)
    }


    private fun initializeGrid(gridLayout: GridLayout) {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val tileSize = screenWidth / gridsizevalue

        gridLayout.columnCount = gridsizevalue
        gridLayout.rowCount = gridsizevalue
        if(maxGames==1){
            powerup1Button.visibility = View.GONE
            powerup2Button.visibility = View.GONE
        }
        else{
            powerup1Button.visibility = View.VISIBLE
            powerup2Button.visibility = View.VISIBLE
        }

        val margin = 8 // Define margin in pixels

        for (i in 0 until gridsizevalue) {
            for (j in 0 until gridsizevalue) {
                gameGrid[i][j] = Tile()
                val button = Button(this).apply {
                    tag = intArrayOf(i, j)
                    setOnClickListener { handleTileClick(it.tag as IntArray) }
                    width = tileSize
                    height = tileSize
                    background = ContextCompat.getDrawable(context, R.drawable.rounded_tile)

                    // Set margin programmatically
                    val params = GridLayout.LayoutParams()
                    params.width = tileSize
                    params.height = tileSize
                    params.setMargins(margin, margin, margin, margin) // left, top, right, bottom
                    layoutParams = params
                }
                gridLayout.addView(button)
            }
        }
    }

    private fun handleTileClick(position: IntArray) {
        val (row, col) = position
        val tile = gameGrid[row][col]

        if (isValidMove(tile)) {
            updateTile(tile, row, col)
            checkForExpansion(row, col)
            checkGameOver()
            isPlayer1Turn = !isPlayer1Turn
            updateBackgroundColor()
            updatePoints()

            // Rotate the grid after each turn
            val gridLayout = findViewById<GridLayout>(R.id.gameGrid)
            rotateGrid(gridLayout)
        }
    }


    private fun isValidMove(tile: Tile): Boolean {
        return tile.color == 0 || (isPlayer1Turn && tile.color == 1) || (!isPlayer1Turn && tile.color == 2)
    }

    private fun updateTile(tile: Tile, row: Int, col: Int) {
        if (tile.color == 0) {
            tile.color = if (isPlayer1Turn) 1 else 2
            tile.points = 3
        } else {
            tile.points++
        }
        moveCount++
    }

    private fun checkForExpansion(row: Int, col: Int) {
        if (gameGrid[row][col].points >= 4) {
            expandTile(row, col)
        }
    }

    private fun expandTile(row: Int, col: Int) {
        val origcolor=gameGrid[row][col].color

        gameGrid[row][col].apply {
            points = 0
            color = 0
        }

        // Determine which power-up button is active
        val powerUpButton = if (previousLoser == playerName1) powerup2Button else powerup1Button
        val powerUpColor= if(previousLoser==playerName1) 1 else 2

        if (maxGames != 1) {
            if (!powerUpUsed) {
                if (powerUpButton.isChecked) {
                    if(origcolor==powerUpColor){
                        mediaPlayer.start()
                        spreadToNeighbor(row - 1, col)
                        spreadToNeighbor(row - 2, col)
                        spreadToNeighbor(row + 1, col) // Down
                        spreadToNeighbor(row + 2, col) // Down
                        spreadToNeighbor(row, col - 1) // Left
                        spreadToNeighbor(row, col - 2) // Left
                        spreadToNeighbor(row, col + 1) // Right
                        spreadToNeighbor(row, col + 2) // Right

                        powerUpUsed = true
                        powerup1Button.visibility= View.GONE
                        powerup2Button.visibility= View.GONE
                    }
                    else{
                        spreadToNeighbor(row - 1, col) // Up
                        spreadToNeighbor(row + 1, col) // Down
                        spreadToNeighbor(row, col - 1) // Left
                        spreadToNeighbor(row, col + 1)
                    }
                } else {
                    // Default behavior without power-up
                    spreadToNeighbor(row - 1, col) // Up
                    spreadToNeighbor(row + 1, col) // Down
                    spreadToNeighbor(row, col - 1) // Left
                    spreadToNeighbor(row, col + 1) // Right
                }
            } else {
                // Default behavior without power-up if it has already been used
                spreadToNeighbor(row - 1, col) // Up
                spreadToNeighbor(row + 1, col) // Down
                spreadToNeighbor(row, col - 1) // Left
                spreadToNeighbor(row, col + 1) // Right
            }
        } else {
            // For BEST_OF_ONE tournament type, hide power-up buttons and apply default behavior
            powerup1Button.visibility = View.GONE
            powerup2Button.visibility = View.GONE


            // Default behavior without power-up
            spreadToNeighbor(row - 1, col) // Up
            spreadToNeighbor(row + 1, col) // Down
            spreadToNeighbor(row, col - 1) // Left
            spreadToNeighbor(row, col + 1) // Right
        }
    }


    private fun spreadToNeighbor(row: Int, col: Int) {
        if (row in 0 until gridsizevalue && col in 0 until gridsizevalue) {
            val neighbor = gameGrid[row][col]
            val currentPlayerColor = if (isPlayer1Turn) 1 else 2
            neighbor.color = currentPlayerColor
            neighbor.points++
            if (neighbor.points >= 4) {
                expandTile(row, col)
            }
        }
    }

    private fun checkGameOver() {
        var player1Exists = false
        var player2Exists = false

        for (i in 0 until gridsizevalue) {
            for (j in 0 until gridsizevalue) {
                when (gameGrid[i][j].color) {
                    1 -> player1Exists = true
                    2 -> player2Exists = true
                }
            }
        }

        if ((moveCount > 2) && (!player1Exists || !player2Exists)) {
            val winner = if (player1Exists) playerName1 else playerName2
            val loser = if (player1Exists) playerName2 else playerName1

            previousLoser = loser

            // Update wins
            if (player1Exists) player1Wins++ else player2Wins++

            showGameOverDialog(if (player1Exists) "$playerName1 Wins!" else "$playerName2 Wins!")
        }
    }


    private fun showGameOverDialog(message: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialogue_game_over, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.setCancelable(false) // Prevent the dialog from being dismissed by tapping outside

        // Set the custom view's title and message
        val gameOverMessage = dialogView.findViewById<TextView>(R.id.gameOverMessage)
        val okButton = dialogView.findViewById<Button>(R.id.okButton)
        val homeButton = dialogView.findViewById<Button>(R.id.homeButton)

        gameOverMessage.text = message

        if (maxGames == 1) {
            homeButton.visibility = View.VISIBLE
            homeButton.setOnClickListener {
                dialog.dismiss()
                val intent = Intent(this, FrontPageActivity::class.java)
                startActivity(intent)
                finish() // Finish the current activity to remove it from the back stack
            }
        } else {
            homeButton.visibility = View.GONE
            okButton.text = "Next Match"
        }

        okButton.setOnClickListener {
            dialog.dismiss()
            if (totalGames < maxGames) {
                when {
                    player1Wins == maxGames / 2 + 1 -> {
                        countredwins++
                        showTournamentResult()
                    }
                    player2Wins == maxGames / 2 + 1 -> {
                        countbluewins++
                        showTournamentResult()
                    }
                    else -> {
                        totalGames++
                        resetGame()
                    }
                }
            } else {
                showTournamentResult()
            }
        }

        dialog.show()
    }



    private fun showTournamentResult() {
        val dialogView = layoutInflater.inflate(R.layout.dialogue_tournament_over, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.setCancelable(false) // Prevent the dialog from being dismissed by tapping outside

        // Set the custom view's title and message
        val tournamentResultMessage = dialogView.findViewById<TextView>(R.id.tournamentOverMessage)
        val okButton = dialogView.findViewById<Button>(R.id.home2Button)
        val newtourn = dialogView.findViewById<Button>(R.id.newtournButton)

        // Determine the winner based on the number of wins
        val winner = if (player1Wins > player2Wins) playerName1 else playerName2
        tournamentResultMessage.text = "Congratulations, $winner! You have won the tournament."

        // Set the OK button action to navigate back to the home screen
        okButton.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, FrontPageActivity::class.java)
            startActivity(intent)
            finish() // Finish the current activity to remove it from the back stack
        }

        // Set the new tournament button action to start a new tournament
        newtourn.setOnClickListener {
            dialog.dismiss()
            startNewTournament()
        }

        dialog.show()
    }


    private fun startNewTournament() {
        totalGames = 0
        player1Wins = 0
        player2Wins = 0
        powerup1Button.visibility = View.GONE
        powerup2Button.visibility = View.GONE

        resetGame()
    }

    private fun updateGrid(playerColor: Int) {
        val gridLayout = findViewById<GridLayout>(R.id.gameGrid)
        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as Button
            val position = button.tag as IntArray
            val tile = gameGrid[position[0]][position[1]]
            updateButtonAppearance(button, tile, playerColor)
        }
    }

    private fun updateButtonAppearance(button: Button, tile: Tile, playerColor: Int,) {
        button.apply {
            text = tile.points.toString()
            setTextColor(Color.WHITE)
            background = ContextCompat.getDrawable(context, R.drawable.button_3d_selector)
            setBackgroundColor(playerColor)

            val drawableResource = when (tile.color) {
                1 -> R.drawable.title_background_player1
                2 -> R.drawable.title_background_player2
                else -> R.drawable.rounded_tile
            }
            background = ContextCompat.getDrawable(context, drawableResource)
        }
    }

    private fun resetGame() {
        moveCount = 0
        for (i in 0 until gridsizevalue) {
            for (j in 0 until gridsizevalue) {
                gameGrid[i][j] = Tile()
            }
        }
        isPlayer1Turn = true
        updateBackgroundColor()
        updatePoints()

        powerUpUsed = false

        powerup1Button.visibility = View.VISIBLE
        powerup2Button.visibility = View.VISIBLE
        if (totalGames == 1) {
            powerup1Button.visibility = View.GONE
            powerup2Button.visibility = View.GONE
        }
        if (previousLoser == playerName1) {
            powerup1Button.isChecked = true
        } else {
            powerup2Button.isChecked = true
        }
    }


    private fun updatePoints() {
        var player1Points = 0
        var player2Points = 0

        for (i in 0 until gridsizevalue) {
            for (j in 0 until gridsizevalue) {
                when (gameGrid[i][j].color) {
                    1 -> player1Points += gameGrid[i][j].points
                    2 -> player2Points += gameGrid[i][j].points
                }
            }
        }

        player1PointsTextView.text = player1Points.toString()
        player2PointsTextView.text = player2Points.toString()
    }

    private fun showGameRulesDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialogue_gamerules, null)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun rotateGrid(gridLayout: GridLayout) {
        val currentRotation = gridLayout.rotation
        val newRotation = (currentRotation + 180) % 360
        gridLayout.rotation = newRotation
    }
    private fun navigateToFrontPage() {
        val intent = Intent(this, FrontPageActivity::class.java)
        intent.putExtra("redWins", countredwins)
        intent.putExtra("blueWins", countbluewins)
        startActivity(intent)
    }

}


