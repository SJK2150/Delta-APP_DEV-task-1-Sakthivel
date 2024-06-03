package com.example.deltaapp.model

import android.icu.text.ListFormatter.Width

data class Tile(
    var points: Int = 0,
    var color: Int = 0,
    // 0 for no color, 1 for player 1, 2 for player 2
)