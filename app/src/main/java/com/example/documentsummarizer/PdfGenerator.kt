package com.example.documentsummarizer

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator {
    
    fun generateSummaryPdf(
        context: Context,
        summaryText: String,
        fileName: String = "summary",
        method: String = "Unknown"
    ): File? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val txtFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "${fileName}_${timestamp}.txt")
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val content = "Document Summary\n" +
                    "==================\n\n" +
                    "Generated: ${dateFormat.format(Date())}\n" +
                    "Method: $method\n\n" +
                    "Summary:\n" +
                    "--------\n" +
                    summaryText
            
            FileWriter(txtFile).use { writer ->
                writer.write(content)
            }
            
            txtFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}