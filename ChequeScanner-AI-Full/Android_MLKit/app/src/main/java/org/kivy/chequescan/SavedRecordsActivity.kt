package org.kivy.chequescan

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream

class SavedRecordsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_records)
        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }
        val tv = findViewById<TextView>(R.id.tv_records)
        val file = ExcelHelper.getXlsxFile(this)
        if (!file.exists()) {
            tv.text = "No records yet"
            return
        }
        try {
            val fis = FileInputStream(file)
            val wb = XSSFWorkbook(fis)
            val sheet = wb.getSheetAt(0)
            val sb = StringBuilder()
            for (r in 1..sheet.lastRowNum) {
                val row = sheet.getRow(r)
                val cno = row.getCell(0)?.stringCellValue ?: ""
                val date = row.getCell(1)?.stringCellValue ?: ""
                val amount = row.getCell(3)?.stringCellValue ?: ""
                sb.append("#${'$'}{cno} — ${'$'}{date} — ₹${'$'}{amount}\n")
            }
            tv.text = sb.toString()
            wb.close()
            fis.close()
        } catch (e: Exception) {
            tv.text = "Error reading records"
            e.printStackTrace()
        }
    }
}
