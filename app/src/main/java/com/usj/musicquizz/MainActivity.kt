package com.usj.musicquizz

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.usj.musicquizz.api.SongsServiceApi
import com.usj.musicquizz.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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