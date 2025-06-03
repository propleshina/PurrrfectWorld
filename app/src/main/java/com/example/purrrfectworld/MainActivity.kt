package com.example.purrrfectworld

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.purrrfectworld.GameActivity

class MainActivity : AppCompatActivity() {
    private lateinit var btnInfo: ImageButton
    private lateinit var btnPlay: ImageButton
    private lateinit var btnSaves: ImageButton
    private lateinit var btnSettings: ImageButton
    private var mediaPlayer: MediaPlayer? = null

    // Здесь вы можете добавить переменную для хранения скорости прокрутки
    private var scrollSpeed: Int = 50 // Значение по умолчанию

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация кнопок
        btnInfo = findViewById(R.id.button_info)
        btnPlay = findViewById(R.id.button_play)
        btnSaves = findViewById(R.id.button_saves)
        btnSettings = findViewById(R.id.button_settings)

        // Загрузка настроек скорости прокрутки
        loadScrollSpeed()

        // Инициализация и запуск MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.defolt_music)
        mediaPlayer?.isLooping = true // Включение зацикливания
        mediaPlayer?.setVolume(0.3f, 0.3f)
        mediaPlayer?.start() // Запуск музыки

        // Установка слушателей на кнопки
        btnInfo.setOnClickListener {
            playClickSound()
            showInfoDialog()
        }

        btnPlay.setOnClickListener {
            playClickSound()
            openGameWindow()
        }

        btnSaves.setOnClickListener {
            playClickSound()
            openSavesWindow()
        }

        btnSettings.setOnClickListener {
            playClickSound()
            openSettingsWindow()
        }
    }

    private fun loadScrollSpeed() {
        val prefs = getSharedPreferences("YOUR_PREFS_NAME", Context.MODE_PRIVATE) // Замените на ваше имя файла настроек
        scrollSpeed = prefs.getInt("SCROLL_SPEED_KEY", 50) // Замените на ваш ключ
    }

    override fun onDestroy() {
        super.onDestroy()
        // Остановка и освобождение ресурсов MediaPlayer
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    private fun playClickSound() {
        val clickSound = MediaPlayer.create(this, R.raw.click)
        clickSound.setVolume(1.0f, 1.0f)
        clickSound.setOnCompletionListener {
            it.release() // Освобождаем ресурсы после воспроизведения
        }
        clickSound.start()
    }

    private fun showInfoDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_info, null)
        dialogBuilder.setView(dialogView)
            .setTitle(" ")
            .setPositiveButton("Закрыть") { dialog, _ -> dialog.dismiss() }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun openGameWindow() {
        // Останавливаем музыку главного меню
        mediaPlayer?.let {
            it.stop()
            it.release()
            mediaPlayer = null
        }

        val prefs = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        prefs.edit().remove("currentIndex").remove("currentBackground").apply()

        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("SCROLL_SPEED", scrollSpeed)
            putExtra("RESET_PROGRESS", true)
        }
        startActivity(intent)
    }

    private fun openSavesWindow() {
        val intent = Intent(this, SavesActivity::class.java)
        startActivity(intent)
    }

    private fun openSettingsWindow() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.defolt_music)
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(0.3f, 0.3f)
            mediaPlayer?.start()
        }
    }

    private fun saveScrollSpeed(speed: Int) {
        val prefs = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("SCROLL_SPEED_KEY", speed).apply()
    }
}
