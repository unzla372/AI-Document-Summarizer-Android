package com.example.documentsummarizer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    
    private var lastSummary: String = ""
    private var lastMethod: String = ""
    private var documentText: String = ""
    private lateinit var authManager: AuthManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            authManager = AuthManager(this)
            
            // Check authentication
            if (!authManager.isLoggedIn()) {
                navigateToLogin()
                return
            }
            
            setContentView(R.layout.activity_main)
            
            // Welcome message
            val currentUser = authManager.getCurrentUser()
            Toast.makeText(this, "Welcome back, $currentUser!", Toast.LENGTH_SHORT).show()
            
            setupViews()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
    
    private fun setupViews() {
        val btnSelectFile = findViewById<Button>(R.id.btnSelectFile)
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)
        val btnDownloadPdf = findViewById<Button>(R.id.btnDownloadPdf)
        val btnAskQuestion = findViewById<Button>(R.id.btnAskQuestion)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val etQuestion = findViewById<EditText>(R.id.etQuestion)
        val tvSelectedFile = findViewById<TextView>(R.id.tvSelectedFile)
        val spinnerStyle = findViewById<Spinner>(R.id.spinnerStyle)
        val seekBarLength = findViewById<SeekBar>(R.id.seekBarLength)
        val tvMaxLength = findViewById<TextView>(R.id.tvMaxLength)
        
        // Setup spinner
        val styles = arrayOf("Concise", "Detailed", "Bullet Points", "Executive Summary")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, styles)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerStyle.adapter = adapter
        
        // Setup seekbar
        seekBarLength.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val length = progress + 50
                tvMaxLength.text = "Max Summary Length: $length words"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Create sample file
        createSampleFile()
        
        btnSelectFile.setOnClickListener {
            useSampleFile()
        }
        
        btnGenerate.setOnClickListener {
            generateSummary()
        }
        
        btnDownloadPdf.setOnClickListener {
            if (lastSummary.isNotEmpty()) {
                saveSummaryAsPdf(lastSummary, lastMethod)
            }
        }
        
        // Enable ask button when question is entered
        etQuestion.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnAskQuestion.isEnabled = !s.isNullOrBlank() && documentText.isNotEmpty()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        
        btnAskQuestion.setOnClickListener {
            val question = etQuestion.text.toString().trim()
            if (question.isNotEmpty() && documentText.isNotEmpty()) {
                askQuestion(question)
            }
        }
        
        btnLogout.setOnClickListener {
            authManager.logout()
            navigateToLogin()
        }
        
        Toast.makeText(this, "Document Summarizer ready!", Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun createSampleFile() {
        val sampleContent = """AI Document Summarizer - Technical Overview

Introduction:
The AI Document Summarizer is an advanced application that leverages artificial intelligence to automatically generate concise summaries of PDF documents. This tool is designed to help users quickly understand the key points of lengthy documents without reading the entire content.

Key Features:
- PDF text extraction and processing
- Multiple summary styles (Concise, Detailed, Bullet Points, Executive Summary)
- Adjustable summary length controls
- AI-powered question answering system
- Cross-platform compatibility (Web and Android)
- Local fallback functionality when API is unavailable

Technology Stack:
The application uses the phi3 AI model via Ollama for natural language processing. The backend is built with FastAPI (Python) and the mobile app uses native Android development with Kotlin. The web interface is created using Streamlit.

Benefits:
- Saves time by providing quick document insights
- Improves productivity for researchers and professionals
- Supports multiple document formats
- Provides accurate AI-generated summaries
- Enables interactive document exploration through Q&A

Conclusion:
This AI Document Summarizer represents a significant advancement in document processing technology, making information more accessible and digestible for users across various industries."""
        val file = File(filesDir, "sample.txt")
        file.writeText(sampleContent)
    }
    
    private fun generateSummary() {
        val file = File(filesDir, "sample.txt")
        val content = file.readText()
        documentText = content // Store for Q&A
        
        // Show progress
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val cardSummary = findViewById<androidx.cardview.widget.CardView>(R.id.cardSummary)
        val cardQA = findViewById<androidx.cardview.widget.CardView>(R.id.cardQA)
        
        progressBar.visibility = View.VISIBLE
        cardSummary.visibility = View.GONE
        
        // Call API service
        val apiService = ApiService()
        
        // Get settings from UI
        val spinnerStyle = findViewById<Spinner>(R.id.spinnerStyle)
        val seekBarLength = findViewById<SeekBar>(R.id.seekBarLength)
        
        val style = when(spinnerStyle.selectedItemPosition) {
            0 -> "Concise"
            1 -> "Detailed" 
            2 -> "Bullet Points"
            3 -> "Executive Summary"
            else -> "Concise"
        }
        val maxLength = seekBarLength.progress + 50 // 50-500 range
        
        apiService.summarizeText(content, style, maxLength) { result, isApiUsed ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (result != null) {
                    val method = if (isApiUsed) "Ollama (phi3)" else "Local Fallback"
                    showSummary(result, method)
                } else {
                    Toast.makeText(this, "Error generating summary", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun showSummary(summary: String, method: String) {
        val tvSummaryText = findViewById<TextView>(R.id.tvSummaryText)
        val cardSummary = findViewById<androidx.cardview.widget.CardView>(R.id.cardSummary)
        val cardQA = findViewById<androidx.cardview.widget.CardView>(R.id.cardQA)
        val btnShare = findViewById<Button>(R.id.btnShare)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnDownloadPdf = findViewById<Button>(R.id.btnDownloadPdf)
        
        // Store summary for download button
        lastSummary = summary
        lastMethod = method
        
        // Add method info to summary
        val summaryWithMethod = "Method: $method\n\n$summary"
        tvSummaryText.text = summaryWithMethod
        
        // Make TextView scrollable
        tvSummaryText.movementMethod = android.text.method.ScrollingMovementMethod()
        
        cardSummary.visibility = View.VISIBLE
        cardQA.visibility = View.VISIBLE // Show Q&A section
        
        // Show and enable download button
        btnDownloadPdf.visibility = View.VISIBLE
        btnDownloadPdf.isEnabled = true
        
        btnShare.setOnClickListener {
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, summaryWithMethod)
            }
            startActivity(android.content.Intent.createChooser(shareIntent, "Share Summary"))
        }
        
        btnSave.setOnClickListener {
            saveSummaryAsPdf(summary, method)
        }
        
        Toast.makeText(this, "Summary generated using: $method", Toast.LENGTH_SHORT).show()
    }
    
    private fun useSampleFile() {
        val file = File(filesDir, "sample.txt")
        val tvSelectedFile = findViewById<TextView>(R.id.tvSelectedFile)
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)
        
        tvSelectedFile.text = "Selected: sample.txt"
        tvSelectedFile.visibility = View.VISIBLE
        btnGenerate.isEnabled = true
        
        Toast.makeText(this, "Sample file loaded!", Toast.LENGTH_SHORT).show()
    }
    
    private fun saveSummaryAsPdf(summary: String, method: String) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "summary_${timestamp}.txt"
            val file = File(filesDir, fileName)
            
            val content = "Document Summary\n" +
                    "==================\n\n" +
                    "Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n" +
                    "Method: $method\n\n" +
                    "Summary:\n" +
                    "--------\n" +
                    summary
            
            file.writeText(content)
            Toast.makeText(this, "Summary saved as $fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun askQuestion(question: String) {
        val apiService = ApiService()
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvAnswer = findViewById<TextView>(R.id.tvAnswer)
        val btnAskQuestion = findViewById<Button>(R.id.btnAskQuestion)
        
        progressBar.visibility = View.VISIBLE
        btnAskQuestion.isEnabled = false
        
        apiService.answerQuestion(documentText, question) { answer, isApiUsed ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                btnAskQuestion.isEnabled = true
                
                if (answer != null) {
                    val method = if (isApiUsed) "Ollama (phi3)" else "Local"
                    val answerWithMethod = "Method: $method\n\n$answer"
                    tvAnswer.text = answerWithMethod
                    tvAnswer.movementMethod = android.text.method.ScrollingMovementMethod()
                    tvAnswer.visibility = View.VISIBLE
                    
                    Toast.makeText(this, "Answer generated using: $method", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error generating answer", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}