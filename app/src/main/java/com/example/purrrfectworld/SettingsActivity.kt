package com.example.purrrfectworld

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.SeekBar
import com.google.android.material.slider.Slider

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnBack : Button

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        btnBack = findViewById(R.id.button_back)
        btnBack.setOnClickListener { finish() }

        val volumeSeekBar = findViewById<SeekBar>(R.id.volume_seekbar)
        val scrollSpeedSeekBar= findViewById<SeekBar>(R.id.scroll_speed_seekbar)
        val fontSizeSeekBar= findViewById<SeekBar>(R.id.font_size_seekbar)

        // Можно добавить слушатели для сохранения настроек или их применения

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar : SeekBar?, progress : Int, fromUser : Boolean) {
                // Обработка изменения громкости звука
            }
            override fun onStartTrackingTouch(seekBar : SeekBar?) {}
            override fun onStopTrackingTouch(seekBar : SeekBar?) {}
        })

        scrollSpeedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar : SeekBar?, progress : Int, fromUser : Boolean) {
                // Обработка изменения скорости перемотки текста
            }
            override fun onStartTrackingTouch(seekBar : SeekBar?) {}
            override fun onStopTrackingTouch(seekBar : SeekBar?) {}
        })

        fontSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar : SeekBar?, progress : Int, fromUser : Boolean) {
                // Обработка изменения размера текста
            }
            override fun onStartTrackingTouch(seekBar : SeekBar?) {}
            override fun onStopTrackingTouch(seekBar : SeekBar?) {}
        })

    }
}