package com.ksp.couriercompanion.ui

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.ksp.couriercompanion.data.AppDatabase
import com.ksp.couriercompanion.importer.MaxymoImportParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImportActivity : ComponentActivity() {
    private lateinit var status: TextView

    private val picker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult

        lifecycleScope.launch {
            val content = withContext(Dispatchers.IO) {
                contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            val offers = MaxymoImportParser.parse(content)
            withContext(Dispatchers.IO) {
                AppDatabase.get(this@ImportActivity).offerDao().insertAll(offers)
            }

            status.text = "Imported ${offers.size} Maxymo history rows."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Maxymo History Import"
            textSize = 22f
        }

        status = TextView(this).apply {
            text = "Choose a CSV, TXT, or exported history file."
            textSize = 16f
        }

        val button = Button(this).apply {
            text = "Select Maxymo export"
            setOnClickListener {
                picker.launch("*/*")
            }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(button)

        setContentView(ScrollView(this).apply { addView(layout) })
    }
}
