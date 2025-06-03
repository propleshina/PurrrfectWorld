package com.example.purrrfectworld

import android.media.AudioManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.SeekBar
import android.media.MediaPlayer

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var audioManager: AudioManager

    private val PREFS_NAME = "AppSettings"
    private val VOLUME_KEY = "volume"
    private val SCROLL_SPEED_KEY = "scroll_speed"
    private val FONT_SIZE_KEY = "font_size"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Инициализация AudioManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        btnBack = findViewById(R.id.button_back)
        btnBack.setOnClickListener {
            playClickSound()
            finish() }

        val volumeSeekBar = findViewById<SeekBar>(R.id.volume_seekbar)
        val scrollSpeedSeekBar = findViewById<SeekBar>(R.id.scroll_speed_seekbar)
        val fontSizeSeekBar = findViewById<SeekBar>(R.id.font_size_seekbar)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Получаем текущее значение громкости системы
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        // Устанавливаем значение SeekBar в диапазоне от 0 до 100
        volumeSeekBar.progress = (currentVolume.toFloat() / maxVolume * 100).toInt()

        // Установка значений из SharedPreferences
        scrollSpeedSeekBar.progress = prefs.getInt(SCROLL_SPEED_KEY, 50) // Значение по умолчанию
        fontSizeSeekBar.progress = prefs.getInt(FONT_SIZE_KEY, 14)

        // Установка начальной громкости системы
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                savePreference(VOLUME_KEY, progress)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (progress / 100.0 * maxVolume).toInt(), 0)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        scrollSpeedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                savePreference(SCROLL_SPEED_KEY, progress)

            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        fontSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                savePreference(FONT_SIZE_KEY, progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun playClickSound() {
        val clickSound = MediaPlayer.create(this, R.raw.click)
        clickSound.setVolume(1.0f, 1.0f)
        clickSound.setOnCompletionListener {
            it.release() // Освобождаем ресурсы после воспроизведения
        }
        clickSound.start()
    }

    private fun savePreference(key: String, value: Int) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putInt(key, value)
            apply()
        }
    }
}