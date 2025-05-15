package com.example.purrrfectworld

import com.example.purrrfectworld.R
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var btnInfo: ImageButton
    private lateinit var btnPlay: Button
    private lateinit var btnSaves: Button
    private lateinit var btnSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация кнопок
        btnInfo = findViewById(R.id.button_info)
        btnPlay = findViewById(R.id.button_play)
        btnSaves = findViewById(R.id.button_saves)
        btnSettings = findViewById(R.id.button_settings)

        // Обработчики кнопок
        btnInfo.setOnClickListener { showInfoDialog() }
        btnPlay.setOnClickListener { openGameWindow() }
        btnSaves.setOnClickListener { openSavesWindow() }
        btnSettings.setOnClickListener { openSettingsWindow() }
    }

    private fun showInfoDialog() {
        // Создаем новый диалог
        val dialogBuilder = AlertDialog.Builder(this)

        // Устанавливаем разметку из XML
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_info, null) // Замените dialog_info на имя вашего XML-файла
        dialogBuilder.setView(dialogView)

        // Настраиваем кнопки диалога
        dialogBuilder.setTitle(" ")
        dialogBuilder.setPositiveButton("Закрыть") { dialog, _ ->
            dialog.dismiss()
        }

        // Создаем и показываем диалог
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