package com.example.purrrfectworld

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class SavesActivity : AppCompatActivity() {

    private val PREFS_NAME = "SavesPrefs"
    private val SLOT_COUNT = 9

    private lateinit var savedSlots: MutableList<SaveSlotData?>
    private val saveSlots = mutableListOf<SaveSlot>() // список слотов

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saves)

        val container = findViewById<GridLayout>(R.id.slots_container)
        val btnBack = findViewById<Button>(R.id.button_back)

        // Загружаем сохранения из SharedPreferences
        savedSlots = loadAllSlots()

        // Создаём и инициализируем слоты
        for (i in 0 until SLOT_COUNT) {
            val slotView: View = LayoutInflater.from(this).inflate(R.layout.item_save_slot, null)

            // Устанавливаем параметры layout для корректного отображения в GridLayout
            val params = GridLayout.LayoutParams()
            params.width = GridLayout.LayoutParams.WRAP_CONTENT
            params.height = GridLayout.LayoutParams.WRAP_CONTENT
            slotView.layoutParams = params

            val slot = SaveSlot(i, slotView)
            slot.setup()
            saveSlots.add(slot)

            // Добавляем слот в контейнер
            container.addView(slotView)

            // Обновляем слот данными из сохранений (если есть)
            savedSlots.getOrNull(i)?.let {
                slot.updateSlot(true, it.title, it.date)
            } ?: run {
                // Если данных нет - показываем пустой слот
                slot.updateSlot(false, "Пусто", "")
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun saveGame(saveName: String) {
        val indexToSave = findFirstEmptySlot() ?: 0 // или логика выбора другого слота
        savedSlots[indexToSave] = SaveSlotData(saveName, getCurrentDateTime())
        saveSlots[indexToSave].updateSlot(true, saveName, getCurrentDateTime())
        saveAllSlots(savedSlots)
        Toast.makeText(this, "Игра сохранена в слот $indexToSave", Toast.LENGTH_SHORT).show()
    }

    private fun findFirstEmptySlot(): Int? {
        val index = savedSlots.indexOfFirst { it == null }
        return if (index >= 0) index else null
    }

    private fun getCurrentDateTime(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    inner class SaveSlot(val index: Int, private val view: View) {
        private var isLongPress = false

        fun setup() {
            val cardView = view.findViewById<LinearLayout>(R.id.save_card)
            val titleText = view.findViewById<TextView>(R.id.title)

            // Изначально слот пустой или с данными из сохранений
            val data = savedSlots.getOrNull(index)
            if (data != null) {
                updateSlot(true, data.title, data.date)
            } else {
                updateSlot(false, "Пусто", "")
            }

            view.setOnClickListener {
                if (!isLongPress) {
                    Toast.makeText(view.context, "Запуск игры из слота $index", Toast.LENGTH_SHORT).show()
                }
                isLongPress = false
            }

            view.setOnLongClickListener {
                isLongPress = true
                AlertDialog.Builder(view.context)
                    .setTitle("Удалить сохранение?")
                    .setMessage("Вы действительно хотите удалить это сохранение?")
                    .setPositiveButton("Удалить") { _, _ ->
                        // Удаление сохранения
                        savedSlots[index] = null
                        updateSlot(false, "Пусто", "")
                        saveAllSlots(savedSlots)
                    }
                    .setNegativeButton("Отмена") { _, _ -> }
                    .setOnDismissListener { isLongPress = false }
                    .show()
                true
            }
        }

        fun updateSlot(hasData: Boolean, titleStr: String, dateStr: String) {
            val card = view.findViewById<LinearLayout>(R.id.save_card)
            val titleText = view.findViewById<TextView>(R.id.title)

            if (hasData) {
                card.setBackgroundResource(R.drawable.save_slot_background) // фон с изображением сохранения
                titleText.text = "$titleStr\n$dateStr"
            } else {
                card.setBackgroundResource(android.R.color.transparent)
                titleText.text = "Пусто"
            }
        }
    }

    private fun saveAllSlots(slots: List<SaveSlotData?>) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        for (i in 0 until SLOT_COUNT) {
            val slotData = slots.getOrNull(i)
            if (slotData != null) {
                editor.putString("slot_title_$i", slotData.title)
                editor.putString("slot_date_$i", slotData.date)
            } else {
                editor.remove("slot_title_$i")
                editor.remove("slot_date_$i")
            }
        }
        editor.apply()
    }

    private fun loadAllSlots(): MutableList<SaveSlotData?> {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val list = mutableListOf<SaveSlotData?>()
        for (i in 0 until SLOT_COUNT) {
            val titleKey = "slot_title_$i"
            val dateKey = "slot_date_$i"

            if (prefs.contains(titleKey) && prefs.contains(dateKey)) {
                val title = prefs.getString(titleKey, "") ?: ""
                val date  = prefs.getString(dateKey, "") ?: ""
                list.add(SaveSlotData(title, date))
            } else {
                list.add(null)
            }
        }
        return list
    }

    data class SaveSlotData(val title: String, val date: String)
}