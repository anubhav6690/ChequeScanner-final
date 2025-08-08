package org.kivy.chequescan

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.CellType

object ExcelHelper {
    fun getStorageDir(context: Context): File {
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val dir = File(downloads, "ChequeScanner")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getXlsxFile(context: Context): File {
        return File(getStorageDir(context), "cheque_records.xlsx")
    }

    fun appendRow(context: Context, chequeNo: String, date: String, bank: String, amount: String, payee: String) {
        try {
            val file = getXlsxFile(context)
            val workbook = if (file.exists()) {
                XSSFWorkbook(file.inputStream())
            } else {
                XSSFWorkbook().apply {
                    val sheet = createSheet("Records")
                    val header = sheet.createRow(0)
                    header.createCell(0).setCellValue("Cheque No")
                    header.createCell(1).setCellValue("Date")
                    header.createCell(2).setCellValue("Bank Name")
                    header.createCell(3).setCellValue("Amount")
                    header.createCell(4).setCellValue("Payee Name")
                    header.createCell(5).setCellValue("Timestamp")
                }
            }

            val sheet = workbook.getSheetAt(0)
            val newRowNum = sheet.lastRowNum + 1
            val row = sheet.createRow(newRowNum)
            row.createCell(0, CellType.STRING).setCellValue(chequeNo)
            row.createCell(1, CellType.STRING).setCellValue(date)
            row.createCell(2, CellType.STRING).setCellValue(bank)
            row.createCell(3, CellType.STRING).setCellValue(amount)
            row.createCell(4, CellType.STRING).setCellValue(payee)
            val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            row.createCell(5, CellType.STRING).setCellValue(ts)

            val out = FileOutputStream(file)
            workbook.write(out)
            out.close()
            workbook.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
