package com.example.documentsummarizer

data class SummaryResult(
    val summary: String,
    val processingTime: Double,
    val wordCount: Int,
    val compressionRatio: Double
)