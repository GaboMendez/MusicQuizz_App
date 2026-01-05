package com.usj.musicquizz

import android.app.Application
import com.usj.musicquizz.model.Song

class MusicQuizzApp : Application() {
    
    var songs: List<Song> = emptyList()
    var playerName: String = ""
    
    companion object {
        const val TOTAL_QUESTIONS = 10
        const val BASE_POINTS = 1000
        const val TIME_BONUS_MAX = 500
        const val STREAK_BONUS = 100
        const val MAX_TIME_SECONDS = 15
    }
}
