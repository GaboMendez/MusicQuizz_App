package com.usj.musicquizz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.usj.musicquizz.databinding.ActivityResultsBinding

class ResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val score = intent.getIntExtra("score", 0)
        val totalQuestions = intent.getIntExtra("total_questions", MusicQuizzApp.TOTAL_QUESTIONS)
        val playerName = (application as MusicQuizzApp).playerName

        binding.congratsText.text = getString(R.string.congrats_player, playerName)
        binding.scoreText.text = score.toString()
        
        // Calculate max possible score for reference
        val maxPossibleScore = totalQuestions * (MusicQuizzApp.BASE_POINTS + MusicQuizzApp.TIME_BONUS_MAX) + 
            (1 until totalQuestions).sum() * MusicQuizzApp.STREAK_BONUS
        
        val percentage = (score.toFloat() / maxPossibleScore * 100).toInt()
        binding.scoreLabel.text = getString(R.string.score_percentage, percentage)

        // Save high score
        saveHighScore(playerName, score)

        binding.playAgainButton.setOnClickListener {
            val intent = Intent(this, NameInputActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        binding.exitButton.setOnClickListener {
            finishAffinity()
        }
    }

    private fun saveHighScore(playerName: String, score: Int) {
        val prefs = getSharedPreferences("music_quizz", MODE_PRIVATE)
        val highScore = prefs.getInt("high_score", 0)
        
        if (score > highScore) {
            prefs.edit()
                .putInt("high_score", score)
                .putString("high_score_player", playerName)
                .apply()
        }
    }
}
