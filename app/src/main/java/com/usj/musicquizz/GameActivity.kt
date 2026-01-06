package com.usj.musicquizz

import android.content.Intent
import android.content.res.ColorStateList
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.usj.musicquizz.databinding.ActivityGameBinding
import com.usj.musicquizz.model.Song

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    private var mediaPlayer: MediaPlayer? = null
    private var isPaused = false
    private var remainingTimeMillis = 0L
    private var countDownTimer: CountDownTimer? = null
    private var seekBarTimer: CountDownTimer? = null

    private var currentQuestion = 0
    private var currentScore = 0
    private var currentStreak = 0
    private var bestStreak = 0
    private var correctAnswers = 0
    private var questionStartTime = 0L
    private var gameStartTime = 0L
    private var quizSongs: List<Song> = emptyList()
    private var currentOptions: List<Song> = emptyList()
    private var correctAnswer: Song? = null
    private var isAnswered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        
        val isNewGame = intent.getBooleanExtra("new_game", true)
        if (isNewGame) {
            startNewGame()
        } else {
            loadSavedGame()
        }
    }

    private fun initViews() {
        binding.seekBar.isEnabled = false
        
        binding.optionA.setOnClickListener { checkAnswer(0) }
        binding.optionB.setOnClickListener { checkAnswer(1) }
        binding.optionC.setOnClickListener { checkAnswer(2) }
        binding.optionD.setOnClickListener { checkAnswer(3) }
        
        binding.pauseButton.setOnClickListener { togglePause() }
        binding.stopButton.setOnClickListener { stopGame() }
    }
    
    private fun togglePause() {
        if (isPaused) {
            resumeGame()
        } else {
            pauseGame()
        }
    }
    
    private fun pauseGame() {
        isPaused = true
        binding.pauseButton.text = getString(R.string.resume_game)
        binding.pauseButton.setIconResource(android.R.drawable.ic_media_play)
        
        // Pause media player
        mediaPlayer?.pause()
        
        // Cancel and save remaining time
        countDownTimer?.cancel()
        seekBarTimer?.cancel()
        
        // Disable answer buttons while paused
        enableButtons(false)
    }
    
    private fun resumeGame() {
        isPaused = false
        binding.pauseButton.text = getString(R.string.pause_game)
        binding.pauseButton.setIconResource(android.R.drawable.ic_media_pause)
        
        // Resume media player
        mediaPlayer?.start()
        
        // Restart timer with remaining time
        startTimerWithRemaining(remainingTimeMillis)
        
        // Re-enable answer buttons if not already answered
        if (!isAnswered) {
            enableButtons(true)
        }
    }
    
    private fun stopGame() {
        // Save current state and go back to name input
        saveGameState()
        stopMediaPlayer()
        countDownTimer?.cancel()
        seekBarTimer?.cancel()
        
        val intent = Intent(this, NameInputActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
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
        bestStreak = 0
        correctAnswers = 0
        gameStartTime = System.currentTimeMillis()
        
        saveGameState()
        loadQuestion()
    }

    private fun loadSavedGame() {
        val prefs = getSharedPreferences("music_quizz", MODE_PRIVATE)
        currentQuestion = prefs.getInt("current_question", 0)
        currentScore = prefs.getInt("current_score", 0)
        currentStreak = prefs.getInt("current_streak", 0)
        bestStreak = prefs.getInt("best_streak", 0)
        correctAnswers = prefs.getInt("correct_answers", 0)
        gameStartTime = prefs.getLong("game_start_time", System.currentTimeMillis())
        
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
            .putInt("best_streak", bestStreak)
            .putInt("correct_answers", correctAnswers)
            .putLong("game_start_time", gameStartTime)
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
        binding.questionText.text = getString(R.string.which_song)
        binding.pointsText.text = currentScore.toString()
        binding.questionNumberText.text = "${currentQuestion + 1}/${quizSongs.size}"
        
        binding.optionA.text = currentOptions[0].name
        binding.optionB.text = currentOptions[1].name
        binding.optionC.text = currentOptions[2].name
        binding.optionD.text = currentOptions[3].name
        
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
        binding.seekBar.max = mp.duration
        
        seekBarTimer?.cancel()
        seekBarTimer = object : CountDownTimer(MusicQuizzApp.MAX_TIME_SECONDS * 1000L, 100) {
            override fun onTick(millisUntilFinished: Long) {
                if (mediaPlayer?.isPlaying == true) {
                    binding.seekBar.progress = mediaPlayer?.currentPosition ?: 0
                }
            }

            override fun onFinish() {}
        }.start()
    }

    private fun startTimer() {
        remainingTimeMillis = MusicQuizzApp.MAX_TIME_SECONDS * 1000L
        startTimerWithRemaining(remainingTimeMillis)
    }
    
    private fun startTimerWithRemaining(timeMillis: Long) {
        countDownTimer?.cancel()
        
        countDownTimer = object : CountDownTimer(timeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeMillis = millisUntilFinished
                val seconds = (millisUntilFinished / 1000).toInt()
                binding.timerText.text = formatTime(seconds)
            }

            override fun onFinish() {
                remainingTimeMillis = 0L
                binding.timerText.text = formatTime(0)
                if (!isAnswered) {
                    handleTimeout()
                }
            }
        }.start()
        
        // Also restart seekbar timer
        if (mediaPlayer != null) {
            setupSeekBar(mediaPlayer!!)
        }
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
        val buttons = listOf(binding.optionA, binding.optionB, binding.optionC, binding.optionD)
        val correctIndex = currentOptions.indexOfFirst { it.id == correctAnswer?.id }
        
        if (isCorrect) {
            setButtonColor(buttons[optionIndex], R.color.correct_answer)
            
            // Calculate score
            val timeElapsed = (System.currentTimeMillis() - questionStartTime) / 1000.0
            val timeBonus = ((MusicQuizzApp.MAX_TIME_SECONDS - timeElapsed) / MusicQuizzApp.MAX_TIME_SECONDS * MusicQuizzApp.TIME_BONUS_MAX).toInt().coerceAtLeast(0)
            
            currentStreak++
            correctAnswers++
            if (currentStreak > bestStreak) {
                bestStreak = currentStreak
            }
            val streakBonus = (currentStreak - 1) * MusicQuizzApp.STREAK_BONUS
            
            val questionScore = MusicQuizzApp.BASE_POINTS + timeBonus + streakBonus
            currentScore += questionScore
            
            binding.pointsText.text = currentScore.toString()
        } else {
            setButtonColor(buttons[optionIndex], R.color.wrong_answer)
            setButtonColor(buttons[correctIndex], R.color.correct_answer)
            currentStreak = 0
        }
        
        saveGameState()
        
        // Move to next question after a delay
        binding.questionText.postDelayed({
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
        val buttons = listOf(binding.optionA, binding.optionB, binding.optionC, binding.optionD)
        val correctIndex = currentOptions.indexOfFirst { it.id == correctAnswer?.id }
        setButtonColor(buttons[correctIndex], R.color.correct_answer)
        
        saveGameState()
        
        // Move to next question after a delay
        binding.questionText.postDelayed({
            currentQuestion++
            saveGameState()
            loadQuestion()
        }, 2000)
    }

    private fun setButtonColor(button: MaterialButton, colorResId: Int) {
        button.backgroundTintList = ColorStateList.valueOf(getColor(colorResId))
        button.setTextColor(getColor(R.color.white))
        button.strokeWidth = 0
    }

    private fun enableButtons(enabled: Boolean) {
        binding.optionA.isEnabled = enabled
        binding.optionB.isEnabled = enabled
        binding.optionC.isEnabled = enabled
        binding.optionD.isEnabled = enabled
    }

    private fun resetButtonColors() {
        val buttons = listOf(binding.optionA, binding.optionB, binding.optionC, binding.optionD)
        val defaultColor = ColorStateList.valueOf(getColor(R.color.white))
        val strokeColor = ColorStateList.valueOf(getColor(R.color.usj_orange))
        val textColor = getColor(R.color.primary_text)
        val strokeWidthPx = (2 * resources.displayMetrics.density).toInt()

        buttons.forEach { button ->
            button.backgroundTintList = defaultColor
            button.setTextColor(textColor)
            button.strokeColor = strokeColor
            button.strokeWidth = strokeWidthPx
        }
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
        
        // Calculate total time played
        val totalTimeSeconds = ((System.currentTimeMillis() - gameStartTime) / 1000).toInt()
        
        // Navigate to results
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("score", currentScore)
        intent.putExtra("total_questions", quizSongs.size)
        intent.putExtra("correct_answers", correctAnswers)
        intent.putExtra("best_streak", bestStreak)
        intent.putExtra("total_time_seconds", totalTimeSeconds)
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
