package com.example.purrrfectworld

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

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
        mediaPlayer?.start() // Запуск музыки

        // Установка слушателей на кнопки
        btnInfo.setOnClickListener { showInfoDialog() }
        btnPlay.setOnClickListener { openGameWindow() }
        btnSaves.setOnClickListener { openSavesWindow() }
        btnSettings.setOnClickListener { openSettingsWindow() }
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
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("SCROLL_SPEED", scrollSpeed) // Передаем скорость в GameActivity, если нужно
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

    private fun saveScrollSpeed(speed: Int) {
        val prefs = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("SCROLL_SPEED_KEY", speed).apply()
    }
}
