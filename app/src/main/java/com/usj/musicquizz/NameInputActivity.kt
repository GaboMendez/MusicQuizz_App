package com.usj.musicquizz

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.usj.musicquizz.databinding.ActivityNameInputBinding

class NameInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNameInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNameInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if there's a saved game
        val prefs = getSharedPreferences("music_quizz", MODE_PRIVATE)
        val savedName = prefs.getString("player_name", "")
        val hasSavedGame = prefs.getBoolean("has_saved_game", false)

        // Only restore name if there's a game to continue, otherwise clear it
        if (hasSavedGame && !savedName.isNullOrEmpty()) {
            binding.nameEditText.setText(savedName)
        } else {
            binding.nameEditText.setText("")
        }

        binding.continueGameButton.isEnabled = hasSavedGame
        binding.continueGameButton.alpha = if (hasSavedGame) 1.0f else 0.5f

        binding.startGameButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_name_hint), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            savePlayerName(name)
            startNewGame()
        }

        binding.continueGameButton.setOnClickListener {
            // Use saved player name when continuing, not the text field
            val prefs = getSharedPreferences("music_quizz", MODE_PRIVATE)
            val savedName = prefs.getString("player_name", "") ?: ""
            
            if (savedName.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_name_hint), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            (application as MusicQuizzApp).playerName = savedName
            continueGame()
        }
    }

    private fun savePlayerName(name: String) {
        (application as MusicQuizzApp).playerName = name
        getSharedPreferences("music_quizz", MODE_PRIVATE)
            .edit()
            .putString("player_name", name)
            .apply()
    }

    private fun startNewGame() {
        // Clear any saved game state
        getSharedPreferences("music_quizz", MODE_PRIVATE)
            .edit()
            .putBoolean("has_saved_game", false)
            .remove("current_question")
            .remove("current_score")
            .remove("current_streak")
            .remove("best_streak")
            .remove("correct_answers")
            .remove("game_start_time")
            .remove("quiz_songs")
            .apply()

        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("new_game", true)
        startActivity(intent)
    }

    private fun continueGame() {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("new_game", false)
        startActivity(intent)
    }
}
