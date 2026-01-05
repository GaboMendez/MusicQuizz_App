package com.usj.musicquizz

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.usj.musicquizz.api.SongsServiceApi
import com.usj.musicquizz.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    private val songsServiceApi by lazy {
        SongsServiceApi().apply {
            customBaseUrl = "http://10.0.2.2:8080"
        }
    }

    private val scope by lazy {
        CoroutineScope(Dispatchers.IO)
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)

        loadSongs()
    }

    private fun loadSongs() {
        scope.launch {
            try {
                withContext(Dispatchers.Main) {
                    statusText.text = getString(R.string.loading_songs)
                }
                
                val songs = songsServiceApi.findAll()
                
                withContext(Dispatchers.Main) {
                    statusText.text = getString(R.string.songs_loaded, songs.size)
                    
                    // Store songs in application and navigate to name input
                    (application as MusicQuizzApp).songs = songs
                    
                    // Small delay to show the loaded message
                    progressBar.postDelayed({
                        navigateToNameInput()
                    }, 500)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = getString(R.string.error_loading_songs)
                    
                    // Retry after 3 seconds
                    progressBar.postDelayed({
                        loadSongs()
                    }, 3000)
                }
            }
        }
    }

    private fun navigateToNameInput() {
        val intent = Intent(this, NameInputActivity::class.java)
        startActivity(intent)
        finish()
    }
}
