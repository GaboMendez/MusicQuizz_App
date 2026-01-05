package com.usj.musicquizz

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.usj.musicquizz.model.Song

class GameActivity : AppCompatActivity() {

    private lateinit var questionText: TextView
    private lateinit var timerText: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var pointsText: TextView
    private lateinit var questionNumberText: TextView
    private lateinit var optionA: Button
    private lateinit var optionB: Button
    private lateinit var optionC: Button
    private lateinit var optionD: Button

    private var mediaPlayer: MediaPlayer? = null
    private var countDownTimer: CountDownTimer? = null
    private var seekBarTimer: CountDownTimer? = null

    private var currentQuestion = 0
    private var currentScore = 0
    private var currentStreak = 0
    private var questionStartTime = 0L
    private var quizSongs: List<Song> = emptyList()
    private var currentOptions: List<Song> = emptyList()
    private var correctAnswer: Song? = null
    private var isAnswered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        initViews()
        
        val isNewGame = intent.getBooleanExtra("new_game", true)
        if (isNewGame) {
            startNewGame()
        } else {
            loadSavedGame()
        }
    }

    private fun initViews() {
        questionText = findViewById(R.id.questionText)
        timerText = findViewById(R.id.timerText)
        seekBar = findViewById(R.id.seekBar)
        pointsText = findViewById(R.id.pointsText)
        questionNumberText = findViewById(R.id.questionNumberText)
        optionA = findViewById(R.id.optionA)
        optionB = findViewById(R.id.optionB)
        optionC = findViewById(R.id.optionC)
        optionD = findViewById(R.id.optionD)

        seekBar.isEnabled = false
        
        optionA.setOnClickListener { checkAnswer(0) }
        optionB.setOnClickListener { checkAnswer(1) }
        optionC.setOnClickListener { checkAnswer(2) }
        optionD.setOnClickListener { checkAnswer(3) }
    }

    private fun startNewGame() {
        val app = application as MusicQuizzApp
        val allSongs = app.songs.shuffled()
        
        // Select songs for the quiz (max 10 or all available)
        val numQuestions = minOf(MusicQuizzApp.TOTAL_QUESTIONS, allSongs.size)
        quizSongs = allSongs.take(numQuestions)
        
        currentQuestion = 0
        currentScore = 0
        currentStreak = 0
        
        saveGameState()
        loadQuestion()
    }

    private fun loadSavedGame() {
        val prefs = getSharedPreferences("music_quizz", MODE_PRIVATE)
        currentQuestion = prefs.getInt("current_question", 0)
        currentScore = prefs.getInt("current_score", 0)
        currentStreak = prefs.getInt("current_streak", 0)
        
        val savedSongIds = prefs.getString("quiz_songs", "")?.split(",")
            ?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        
        val app = application as MusicQuizzApp
        quizSongs = savedSongIds.mapNotNull { id -> 
            app.songs.find { it.id == id }
        }
        
        if (quizSongs.isEmpty() || currentQuestion >= quizSongs.size) {
            startNewGame()
        } else {
            loadQuestion()
        }
    }

    private fun saveGameState() {
        getSharedPreferences("music_quizz", MODE_PRIVATE)
            .edit()
            .putBoolean("has_saved_game", true)
            .putInt("current_question", currentQuestion)
            .putInt("current_score", currentScore)
            .putInt("current_streak", currentStreak)
            .putString("quiz_songs", quizSongs.mapNotNull { it.id }.joinToString(","))
            .apply()
    }

    private fun loadQuestion() {
        if (currentQuestion >= quizSongs.size) {
            finishGame()
            return
        }

        isAnswered = false
        enableButtons(true)
        resetButtonColors()
        
        correctAnswer = quizSongs[currentQuestion]
        
        // Generate 4 options (1 correct + 3 wrong)
        val app = application as MusicQuizzApp
        val wrongOptions = app.songs
            .filter { it.id != correctAnswer?.id }
            .shuffled()
            .take(3)
        
        currentOptions = (listOf(correctAnswer!!) + wrongOptions).shuffled()
        
        // Update UI
        questionText.text = getString(R.string.which_song)
        pointsText.text = currentScore.toString()
        questionNumberText.text = getString(R.string.question_number, currentQuestion + 1, quizSongs.size)
        
        optionA.text = currentOptions[0].name
        optionB.text = currentOptions[1].name
        optionC.text = currentOptions[2].name
        optionD.text = currentOptions[3].name
        
        // Start playing the song
        playSong(correctAnswer!!)
        
        // Start timer
        startTimer()
        
        questionStartTime = System.currentTimeMillis()
    }

    private fun playSong(song: Song) {
        stopMediaPlayer()
        
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            
            try {
                // Convert localhost URL for emulator
                val url = song.file?.replace("localhost", "10.0.2.2")
                setDataSource(url)
                prepareAsync()
                
                setOnPreparedListener { mp ->
                    mp.start()
                    setupSeekBar(mp)
                }
                
                setOnErrorListener { _, _, _ ->
                    // Handle error silently
                    true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupSeekBar(mp: MediaPlayer) {
        seekBar.max = mp.duration
        
        seekBarTimer?.cancel()
        seekBarTimer = object : CountDownTimer(MusicQuizzApp.MAX_TIME_SECONDS * 1000L, 100) {
            override fun onTick(millisUntilFinished: Long) {
                if (mediaPlayer?.isPlaying == true) {
                    seekBar.progress = mediaPlayer?.currentPosition ?: 0
                }
            }

            override fun onFinish() {}
        }.start()
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        
        countDownTimer = object : CountDownTimer(MusicQuizzApp.MAX_TIME_SECONDS * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                timerText.text = formatTime(seconds)
            }

            override fun onFinish() {
                timerText.text = formatTime(0)
                if (!isAnswered) {
                    handleTimeout()
                }
            }
        }.start()
    }

    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%d:%02d", mins, secs)
    }

    private fun checkAnswer(optionIndex: Int) {
        if (isAnswered) return
        isAnswered = true
        
        countDownTimer?.cancel()
        enableButtons(false)
        
        val selectedSong = currentOptions[optionIndex]
        val isCorrect = selectedSong.id == correctAnswer?.id
        
        // Highlight correct and wrong answers
        val buttons = listOf(optionA, optionB, optionC, optionD)
        val correctIndex = currentOptions.indexOfFirst { it.id == correctAnswer?.id }
        
        if (isCorrect) {
            buttons[optionIndex].setBackgroundColor(getColor(R.color.correct_answer))
            
            // Calculate score
            val timeElapsed = (System.currentTimeMillis() - questionStartTime) / 1000.0
            val timeBonus = ((MusicQuizzApp.MAX_TIME_SECONDS - timeElapsed) / MusicQuizzApp.MAX_TIME_SECONDS * MusicQuizzApp.TIME_BONUS_MAX).toInt().coerceAtLeast(0)
            
            currentStreak++
            val streakBonus = (currentStreak - 1) * MusicQuizzApp.STREAK_BONUS
            
            val questionScore = MusicQuizzApp.BASE_POINTS + timeBonus + streakBonus
            currentScore += questionScore
            
            pointsText.text = currentScore.toString()
        } else {
            buttons[optionIndex].setBackgroundColor(getColor(R.color.wrong_answer))
            buttons[correctIndex].setBackgroundColor(getColor(R.color.correct_answer))
            currentStreak = 0
        }
        
        saveGameState()
        
        // Move to next question after a delay
        questionText.postDelayed({
            currentQuestion++
            saveGameState()
            loadQuestion()
        }, 2000)
    }

    private fun handleTimeout() {
        isAnswered = true
        enableButtons(false)
        currentStreak = 0
        
        // Highlight correct answer
        val buttons = listOf(optionA, optionB, optionC, optionD)
        val correctIndex = currentOptions.indexOfFirst { it.id == correctAnswer?.id }
        buttons[correctIndex].setBackgroundColor(getColor(R.color.correct_answer))
        
        saveGameState()
        
        // Move to next question after a delay
        questionText.postDelayed({
            currentQuestion++
            saveGameState()
            loadQuestion()
        }, 2000)
    }

    private fun enableButtons(enabled: Boolean) {
        optionA.isEnabled = enabled
        optionB.isEnabled = enabled
        optionC.isEnabled = enabled
        optionD.isEnabled = enabled
    }

    private fun resetButtonColors() {
        val defaultColor = getColor(R.color.button_default)
        optionA.setBackgroundColor(defaultColor)
        optionB.setBackgroundColor(defaultColor)
        optionC.setBackgroundColor(defaultColor)
        optionD.setBackgroundColor(defaultColor)
    }

    private fun finishGame() {
        stopMediaPlayer()
        countDownTimer?.cancel()
        seekBarTimer?.cancel()
        
        // Clear saved game
        getSharedPreferences("music_quizz", MODE_PRIVATE)
            .edit()
            .putBoolean("has_saved_game", false)
            .apply()
        
        // Navigate to results
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("score", currentScore)
        intent.putExtra("total_questions", quizSongs.size)
        startActivity(intent)
        finish()
    }

    private fun stopMediaPlayer() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMediaPlayer()
        countDownTimer?.cancel()
        seekBarTimer?.cancel()
    }
}
