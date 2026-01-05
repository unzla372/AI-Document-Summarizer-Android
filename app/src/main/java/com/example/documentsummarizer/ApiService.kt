package com.example.documentsummarizer

import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class ApiService {
    
    private val baseUrl = "http://10.0.2.2:8000" // Android emulator localhost
    
    fun summarizeText(text: String, style: String, maxLength: Int, callback: (String?, Boolean) -> Unit) {
        Thread {
            try {
                // Try direct Ollama connection first
                val ollamaSummary = callOllamaForSummary(text, style, maxLength)
                if (ollamaSummary != null && !ollamaSummary.startsWith("Error:")) {
                    callback(ollamaSummary, true) // true = Ollama used
                    return@Thread
                }
                
                // Fallback to API server
                val url = URL("$baseUrl/summarize")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                
                val jsonBody = JSONObject().apply {
                    put("text", text)
                    put("style", style)
                    put("max_length", maxLength)
                }
                
                connection.outputStream.use { os ->
                    os.write(jsonBody.toString().toByteArray())
                }
                
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(response)
                    val summary = jsonResponse.getString("summary")
                    callback(summary, true) // true = API used
                } else {
                    val localSummary = createLocalSummary(text, maxLength)
                    callback(localSummary, false) // false = local fallback
                }
                
            } catch (e: Exception) {
                // Fallback to local summarization if API fails
                val localSummary = createLocalSummary(text, maxLength)
                callback(localSummary, false) // false = local fallback
            }
        }.start()
    }
    
    private fun callOllamaForSummary(text: String, style: String, maxLength: Int): String? {
        return try {
            val url = URL("http://10.0.2.2:11434/api/generate")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 60000
            
            val stylePrompts = mapOf(
                "Concise" to "Summarize the following text in exactly $maxLength words or less:",
                "Detailed" to "Provide a comprehensive summary in $maxLength words or less covering all main points:",
                "Bullet Points" to "Summarize as key bullet points (use • for bullets) in $maxLength words or less:",
                "Executive Summary" to "Create an executive summary for decision-makers in $maxLength words or less:"
            )
            
            val promptText = stylePrompts[style] ?: stylePrompts["Concise"]!!
            val fullPrompt = "$promptText\n\nText to summarize:\n$text\n\nSummary:"
            
            val jsonBody = JSONObject().apply {
                put("model", "phi3")
                put("prompt", fullPrompt)
                put("stream", false)
                put("options", JSONObject().apply {
                    put("temperature", 0.3)
                    put("num_predict", maxLength * 2)
                })
            }
            
            connection.outputStream.use { os ->
                os.write(jsonBody.toString().toByteArray())
            }
            
            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)
                var summary = jsonResponse.getString("response")
                
                // Enforce word limit
                val words = summary.trim().split("\\s+".toRegex())
                if (words.size > maxLength) {
                    summary = words.take(maxLength).joinToString(" ")
                    if (!summary.endsWith(".") && !summary.endsWith("!") && !summary.endsWith("?")) {
                        summary += "."
                    }
                }
                
                summary
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun answerQuestion(text: String, question: String, callback: (String?, Boolean) -> Unit) {
        Thread {
            try {
                // Try direct Ollama connection first
                val ollamaAnswer = callOllamaDirectly(text, question)
                if (ollamaAnswer != null && !ollamaAnswer.startsWith("Error:")) {
                    callback(ollamaAnswer, true) // true = Ollama used
                    return@Thread
                }
                
                // Fallback to API server
                val url = URL("$baseUrl/answer-question")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                
                val jsonBody = JSONObject().apply {
                    put("text", text)
                    put("question", question)
                }
                
                connection.outputStream.use { os ->
                    os.write(jsonBody.toString().toByteArray())
                }
                
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(response)
                    val answer = jsonResponse.getString("answer")
                    callback(answer, true) // true = API used
                } else {
                    val localAnswer = createLocalAnswer(text, question)
                    callback(localAnswer, false) // false = local fallback
                }
                
            } catch (e: Exception) {
                val localAnswer = createLocalAnswer(text, question)
                callback(localAnswer, false) // false = local fallback
            }
        }.start()
    }
    
    private fun callOllamaDirectly(text: String, question: String): String? {
        return try {
            val url = URL("http://10.0.2.2:11434/api/generate")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout =  60000
            
            val prompt = """You are answering a specific question about a document. Do NOT summarize the document. Only answer the exact question asked.

Document:
$text

Question: $question

Provide a direct answer to the question based only on the information in the document above. If the answer is not in the document, say so.

Answer:"""
            
            val jsonBody = JSONObject().apply {
                put("model", "phi3")
                put("prompt", prompt)
                put("stream", false)
                put("options", JSONObject().apply {
                    put("temperature", 0.3)
                    put("num_predict", 200)
                })
            }
            
            connection.outputStream.use { os ->
                os.write(jsonBody.toString().toByteArray())
            }
            
            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)
                jsonResponse.getString("response")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun createLocalAnswer(text: String, question: String): String {
        // Enhanced document-based question answering
        val questionLower = question.toLowerCase()
        val textLower = text.toLowerCase()
        
        // Extract key terms from the question
        val questionWords = questionLower.split(" ").filter { it.length > 3 && !listOf("what", "how", "why", "when", "where", "who", "the", "and", "are", "is", "this", "that", "with", "for", "from").contains(it) }
        
        // Find sentences that contain question keywords
        val sentences = text.split(".", "\n", "!", "?").map { it.trim() }.filter { it.isNotEmpty() }
        
        val relevantSentences = mutableListOf<String>()
        
        // Score sentences based on keyword matches
        val scoredSentences = sentences.map { sentence ->
            val sentenceLower = sentence.toLowerCase()
            val score = questionWords.count { word -> sentenceLower.contains(word) }
            Pair(sentence, score)
        }.filter { it.second > 0 }.sortedByDescending { it.second }
        
        // Take top 2-3 most relevant sentences
        val topSentences = scoredSentences.take(3).map { it.first }
        
        return if (topSentences.isNotEmpty()) {
            "Based on the document: " + topSentences.joinToString(". ") + "."
        } else {
            // If no direct matches, try to find contextually relevant content
            val contextSentences = sentences.filter { sentence ->
                val sentenceLower = sentence.toLowerCase()
                questionWords.any { word -> 
                    sentenceLower.contains(word.substring(0, minOf(word.length, 4))) // Partial matching
                }
            }.take(2)
            
            if (contextSentences.isNotEmpty()) {
                "From the document: " + contextSentences.joinToString(". ") + "."
            } else {
                "I cannot find specific information in the document to answer this question. Please try rephrasing your question or ask about topics mentioned in the document."
            }
        }
    }
    
    private fun createLocalSummary(text: String, maxLength: Int): String {
        // Simple local summarization with strict length control
        val sentences = text.split(". ")
        var summary = ""
        var wordCount = 0
        
        // Add sentences until we reach word limit
        for (sentence in sentences) {
            val sentenceWords = sentence.trim().split("\\s+".toRegex())
            if (wordCount + sentenceWords.size <= maxLength) {
                summary += sentence.trim() + ". "
                wordCount += sentenceWords.size
            } else {
                // Add partial sentence if there's room
                val remainingWords = maxLength - wordCount
                if (remainingWords > 0) {
                    val partialSentence = sentenceWords.take(remainingWords).joinToString(" ")
                    summary += partialSentence + "."
                }
                break
            }
        }
        
        // Final check - truncate to exact word count if needed
        val finalWords = summary.trim().split("\\s+".toRegex())
        if (finalWords.size > maxLength) {
            summary = finalWords.take(maxLength).joinToString(" ") + "."
        }
        
        return "Local Summary: $summary"
    }
}