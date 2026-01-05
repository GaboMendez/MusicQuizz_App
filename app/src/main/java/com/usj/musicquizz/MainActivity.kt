package com.usj.musicquizz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.usj.musicquizz.api.SongsServiceApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.usj.musicquizz.R

class MainActivity : AppCompatActivity() {

    private val songsServiceApi by lazy {
        SongsServiceApi().apply {
            customBaseUrl = "http://10.0.2.2:8080"
        }
    }

    private val scope by lazy {
        CoroutineScope(Dispatchers.IO)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scope.launch {
            try {
                val songs = songsServiceApi.findAll()
                Log.d("MainActivity", "Songs: $songs")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching songs", e)
            }
        }
    }
}