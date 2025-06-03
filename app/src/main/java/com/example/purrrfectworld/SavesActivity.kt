package com.example.purrrfectworld

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SavesActivity : AppCompatActivity() {

    private val PREFS_NAME = "SavesPrefs"
    private val SLOT_COUNT = 9

    private lateinit var savedSlots: MutableList<SaveSlotData?>
    private val saveSlots = mutableListOf<SaveSlot>()

    private var isSaveMode = false
    private var saveName: String? = null
    private var branch: String? = null
    private var index: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saves)

        val container = findViewById<GridLayout>(R.id.slots_container)
        val btnBack = findViewById<Button>(R.id.button_back)

        // Получаем данные, если это режим сохранения
        isSaveMode = intent.getBooleanExtra("SAVE_MODE", false)
        saveName = intent.getStringExtra("SAVE_NAME")
        branch = intent.getStringExtra("CURRENT_BRANCH")
        index = intent.getIntExtra("CURRENT_INDEX", 0)

        savedSlots = loadAllSlots()

        for (i in 0 until SLOT_COUNT) {
            val slotView = LayoutInflater.from(this).inflate(R.layout.item_save_slot, null)
            val params = GridLayout.LayoutParams()
            params.width = GridLayout.LayoutParams.WRAP_CONTENT
            params.height = GridLayout.LayoutParams.WRAP_CONTENT
            slotView.layoutParams = params

            val slot = SaveSlot(i, slotView)
            slot.setup()
            saveSlots.add(slot)
            container.addView(slotView)

            savedSlots.getOrNull(i)?.let {
                slot.updateSlot(true, it.title, it.date)
            } ?: run {
                slot.updateSlot(false, "Пусто", "")
            }
        }

        btnBack.setOnClickListener {
            playClickSound()
            finish()
        }
    }

    inner class SaveSlot(val index: Int, private val view: View) {
        private var isLongPress = false

        fun setup() {
            val cardView = view.findViewById<LinearLayout>(R.id.save_card)

            view.setOnClickListener {
                playClickSound()

                if (isSaveMode) {
                    if (saveName != null && branch != null) {
                        val date = getCurrentDateTime()
                        savedSlots[index] = SaveSlotData(saveName!!, date)
                        saveAllSlots(savedSlots)

                        // Возвращаем данные обратно
                        val resultIntent = Intent().apply {
                            putExtra("CURRENT_BRANCH", branch)
                            putExtra("CURRENT_INDEX", index)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                } else {
                    // Загрузка
                    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    val savedBranch = prefs.getString("slot_branch_$index", null)
                    val savedIndex = prefs.getInt("slot_index_$index", -1)

                    if (savedBranch != null && savedIndex >= 0) {
                        val resultIntent = Intent().apply {
                            putExtra("CURRENT_BRANCH", savedBranch)
                            putExtra("CURRENT_INDEX", savedIndex)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Toast.makeText(this@SavesActivity, "Слот пуст", Toast.LENGTH_SHORT).show()
                    }
                }

                isLongPress = false
            }

            view.setOnLongClickListener {
                isLongPress = true
                AlertDialog.Builder(view.context).setTitle("Удалить сохранение?")
                    .setMessage("Вы действительно хотите удалить это сохранение?")
                    .setPositiveButton("Удалить") { _, _ ->
                        savedSlots[index] = null
                        updateSlot(false, "Пусто", "")
                        saveAllSlots(savedSlots)
                    }
                    .setNegativeButton("Отмена", null)
                    .setOnDismissListener { isLongPress = false }
                    .show()
                true
            }
        }

        fun updateSlot(hasData: Boolean, titleStr: String, dateStr: String) {
            val card = view.findViewById<LinearLayout>(R.id.save_card)
            val titleText = view.findViewById<TextView>(R.id.title)

            if (hasData) {
                card.setBackgroundResource(R.drawable.save_slot_background)
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
                if (isSaveMode && branch != null) {
                    editor.putString("slot_branch_$i", branch)
                    editor.putInt("slot_index_$i", index)
                }
            } else {
                editor.remove("slot_title_$i")
                editor.remove("slot_date_$i")
                editor.remove("slot_branch_$i")
                editor.remove("slot_index_$i")
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
                val date = prefs.getString(dateKey, "") ?: ""
                list.add(SaveSlotData(title, date))
            } else {
                list.add(null)
            }
        }
        return list
    }

    private fun getCurrentDateTime(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun playClickSound() {
        val clickSound = MediaPlayer.create(this, R.raw.click)
        clickSound.setVolume(1.0f, 1.0f)
        clickSound.setOnCompletionListener {
            it.release()
        }
        clickSound.start()
    }

    data class SaveSlotData(val title: String, val date: String)
}