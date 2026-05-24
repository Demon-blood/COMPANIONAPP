package com.ksp.couriercompanion.ui
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.ksp.couriercompanion.data.AppDatabase
import com.ksp.couriercompanion.importer.MaxymoImportParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImportActivity: ComponentActivity(){
    private lateinit var output:TextView
    private val picker=registerForActivityResult(ActivityResultContracts.OpenDocument()){ uri:Uri? -> uri?.let{ import(it) } }
    override fun onCreate(b:Bundle?){ super.onCreate(b)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(32,48,32,32) }
        output=TextView(this).apply{ text="Choose a Maxymo export file. Supported: CSV, JSON, plain TXT."; textSize=16f }
        root.addView(TextView(this).apply{ text="Maxymo History Import"; textSize=24f })
        root.addView(Button(this).apply{ text="Pick Export File"; setOnClickListener{ picker.launch(arrayOf("text/*","application/json","text/comma-separated-values","application/octet-stream")) } })
        root.addView(output); setContentView(root)
    }
    private fun import(uri:Uri){ lifecycleScope.launch{
        output.text="Importing..."
        val text=withContext(Dispatchers.IO){ contentResolver.openInputStream(uri)?.bufferedReader()?.use{it.readText()} ?: "" }
        val result=withContext(Dispatchers.Default){ MaxymoImportParser.parse(text) }
        withContext(Dispatchers.IO){ AppDatabase.get(this@ImportActivity).offerDao().insertAll(result.imported) }
        output.text="Imported ${result.imported.size} offers. Rejected lines: ${result.rejectedLines}"
    } }
}
