package com.usj.musicquizz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NameInputActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var startGameButton: Button
    private lateinit var continueGameButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_input)

        nameEditText = findViewById(R.id.nameEditText)
        startGameButton = findViewById(R.id.startGameButton)
        continueGameButton = findViewById(R.id.continueGameButton)

        // Check if there's a saved game
        val prefs = getSharedPreferences("music_quizz", MODE_PRIVATE)
        val savedName = prefs.getString("player_name", "")
        val hasSavedGame = prefs.getBoolean("has_saved_game", false)

        if (!savedName.isNullOrEmpty()) {
            nameEditText.setText(savedName)
        }

        continueGameButton.isEnabled = hasSavedGame
        continueGameButton.alpha = if (hasSavedGame) 1.0f else 0.5f

        startGameButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_name_hint), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            savePlayerName(name)
            startNewGame()
        }

        continueGameButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_name_hint), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            savePlayerName(name)
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
