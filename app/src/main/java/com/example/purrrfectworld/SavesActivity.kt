package com.example.purrrfectworld

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class SavesActivity : AppCompatActivity() {

    private val saveSlots = mutableListOf<SaveSlot>() // список слотов

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saves)

        val container = findViewById<GridLayout>(R.id.slots_container)
        val btnBack = findViewById<Button>(R.id.button_back)

        btnBack.setOnClickListener {
            finish()
        }

        // Создаем 9 слотов динамически
        for (i in 0 until 9) {
            val slotView = LayoutInflater.from(this).inflate(R.layout.item_save_slot, container, false)
            val slot = SaveSlot(i, slotView)
            saveSlots.add(slot)
            container.addView(slotView)
            slot.setup()
        }
    }

    inner class SaveSlot(val index: Int, private val view: View) {
        private var isLongPress = false
        private val handler = Handler()

        fun setup() {
            // Инициализация элементов внутри слота
            val card = view.findViewById<MaterialCardView>(R.id.save_card)
            val titleText = view.findViewById<TextView>(R.id.save_title)
            val dateText = view.findViewById<TextView>(R.id.save_date)

            // Изначально пустой слот или с данными (заглушка)
            updateSlot(false, "Пусто", "")

            view.setOnClickListener {
                if (!isLongPress) {
                    // Запуск игры с этого сохранения (заглушка)
                    // TODO: реализовать запуск игры с сохранения
                }
            }

            view.setOnLongClickListener {
                isLongPress = true
                // В диалоге подтверждение удаления
                AlertDialog.Builder(this@SavesActivity)
                    .setTitle("Удалить сохранение?")
                    .setMessage("Вы действительно хотите удалить это сохранение?")
                    .setPositiveButton("Удалить") { _, _ ->
                        // Удаление сохранения (заглушка)
                        updateSlot(false, "Пусто", "")
                    }
                    .setNegativeButton("Отмена") { _, _ -> }
                    .setOnDismissListener { isLongPress = false }
                    .show()
                true
            }

            // Для имитации сохранения можно добавить кнопку или событие (заглушка)
            // Например, при долгом нажатии можно сохранять текущий прогресс.

            // Для теста можно заполнить слот случайными данными:
            // updateSlot(true, "Сохранение ${index + 1}", "Дата/время")

            // В реальности нужно подключить логику сохранений.

            // Пример заполнения:
            if (index % 2 == 0) {
                updateSlot(true, "Сохранение ${index + 1}", "2024-10-23 14:30")
            }
        }

        fun updateSlot(hasData: Boolean, titleStr: String, dateStr: String) {
            val card = view.findViewById<MaterialCardView>(R.id.save_card)
            val titleText = view.findViewById<TextView>(R.id.save_title)
            val dateText = view.findViewById<TextView>(R.id.save_date)

            if (hasData) {
                card.setBackgroundResource(R.drawable.save_slot_background) // фон с изображением сохранения
                titleText.text = titleStr
                dateText.text = dateStr
            } else {
                card.setBackgroundResource(android.R.color.transparent)
                titleText.text = "Пусто"
                dateText.text = ""
            }
        }
    }
}