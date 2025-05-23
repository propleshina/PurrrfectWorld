package com.example.purrrfectworld

import android.app.AlertDialog
import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация кнопок
        btnInfo = findViewById(R.id.button_info)
        btnPlay = findViewById(R.id.button_play)
        btnSaves = findViewById(R.id.button_saves)
        btnSettings = findViewById(R.id.button_settings)

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
}
