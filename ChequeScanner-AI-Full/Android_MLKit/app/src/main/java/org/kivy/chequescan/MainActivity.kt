package org.kivy.chequescan

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ensurePermissions()

        findViewById<Button>(R.id.btn_scan).setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }

        findViewById<Button>(R.id.btn_view).setOnClickListener {
            startActivity(Intent(this, SavedRecordsActivity::class.java))
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bmp = result.data?.extras?.get("data") as? Bitmap
            bmp?.let {
                val savedPath = saveImageToDownloads(it)
                MLKitTextHelper(this).recognizeImage(savedPath) { text ->
                    runOnUiThread {
                        val chequeNo = Regex("\\b\\d{5,7}\\b").find(text)?.value ?: ""
                        val date = Regex("(\\b\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}\\b)").find(text)?.value ?: ""
                        val amount = Regex("([₹Rs.\\s]*\\d{1,3}[,\\d]*)").find(text)?.value ?: ""
                        val bank = listOf("State Bank of India","HDFC Bank","ICICI Bank","Axis Bank","AU Small Finance Bank").find { text.contains(it, ignoreCase = true) } ?: ""
                        val payee = extractPayee(text)
                        showEditDialog(chequeNo, date, bank, amount, payee, savedPath)
                    }
                }
            }
        }
    }

    fun extractPayee(text: String): String {
        val lines = text.split("\\n")
        for (i in lines.indices) {
            if (lines[i].contains("Pay", ignoreCase = true)) {
                if (i+1 < lines.size) return lines[i+1].trim()
            }
        }
        return ""
    }

    fun showEditDialog(chequeNo: String, date: String, bank: String, amount: String, payee: String, imagePath: String) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_edit, null)
        val etCheque = view.findViewById<EditText>(R.id.et_cheque_no)
        val etDate = view.findViewById<EditText>(R.id.et_date)
        val etBank = view.findViewById<EditText>(R.id.et_bank)
        val etAmount = view.findViewById<EditText>(R.id.et_amount)
        val etPayee = view.findViewById<EditText>(R.id.et_payee)
        etCheque.setText(chequeNo)
        etDate.setText(date)
        etBank.setText(bank)
        etAmount.setText(amount)
        etPayee.setText(payee)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Confirm & Save")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val cno = etCheque.text.toString()
                val dt = etDate.text.toString()
                val bk = etBank.text.toString()
                val amt = etAmount.text.toString()
                val py = etPayee.text.toString()
                ExcelHelper.appendRow(this, cno, dt, bk, amt, py)
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    fun ensurePermissions() {
        val required = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        val missing = required.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), PERMISSION_REQUEST)
        }
    }

    fun saveImageToDownloads(bitmap: Bitmap): String {
        val folderName = "ChequeScanner/images"
        val filename = "cheque_" + System.currentTimeMillis() + ".jpg"
        var savedPath = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ChequeScanner/images")
            }
            val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                val out = resolver.openOutputStream(uri)
                out.use { stream -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream) }
                savedPath = uri.toString()
            }
        } else {
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val dir = File(downloads, "ChequeScanner/images")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, filename)
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush(); out.close()
            savedPath = file.absolutePath
        }
        return savedPath
    }
}
