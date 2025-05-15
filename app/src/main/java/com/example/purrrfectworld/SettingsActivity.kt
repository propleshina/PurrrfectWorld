package com.example.purrrfectworld

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.SeekBar

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnBack: Button

    // Названия ключей для SharedPreferences
    private val PREFS_NAME = "AppSettings"
    private val VOLUME_KEY = "volume"
    private val SCROLL_SPEED_KEY = "scroll_speed"
    private val FONT_SIZE_KEY = "font_size"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        btnBack = findViewById(R.id.button_back)
        btnBack.setOnClickListener { finish() }

        val volumeSeekBar = findViewById<SeekBar>(R.id.volume_seekbar)
        val scrollSpeedSeekBar= findViewById<SeekBar>(R.id.scroll_speed_seekbar)
        val fontSizeSeekBar= findViewById<SeekBar>(R.id.font_size_seekbar)

        // Загрузка сохраненных настроек
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        volumeSeekBar.progress = prefs.getInt(VOLUME_KEY, 50) // Значение по умолчанию 50
        scrollSpeedSeekBar.progress = prefs.getInt(SCROLL_SPEED_KEY, 50)
        fontSizeSeekBar.progress = prefs.getInt(FONT_SIZE_KEY, 14)

        // Обработчики для сохранения настроек при изменении
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                savePreference(VOLUME_KEY, progress)
                // Можно добавить код для применения громкости сразу
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        scrollSpeedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                savePreference(SCROLL_SPEED_KEY, progress)
                // Можно добавить код для применения скорости перемотки
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        fontSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                savePreference(FONT_SIZE_KEY, progress)
                // Можно добавить код для применения размера текста
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // Функция для сохранения значения в SharedPreferences
    private fun savePreference(key: String, value: Int) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putInt(key, value)
            apply()
        }
    }
}