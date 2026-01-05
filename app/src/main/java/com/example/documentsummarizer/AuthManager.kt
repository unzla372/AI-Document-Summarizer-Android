package com.example.documentsummarizer

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class AuthManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val userPrefs: SharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    fun signup(username: String, password: String): Boolean {
        // Check if user already exists
        if (userExists(username)) {
            return false
        }
        
        // Hash password
        val hashedPassword = hashPassword(password)
        
        // Store user credentials
        userPrefs.edit()
            .putString("user_$username", hashedPassword)
            .apply()
        
        // Set as current user
        setCurrentUser(username)
        
        return true
    }
    
    fun login(username: String, password: String): Boolean {
        val storedHash = userPrefs.getString("user_$username", null)
        
        if (storedHash == null) {
            return false
        }
        
        val hashedPassword = hashPassword(password)
        
        if (storedHash == hashedPassword) {
            setCurrentUser(username)
            return true
        }
        
        return false
    }
    
    fun logout() {
        prefs.edit()
            .remove(KEY_CURRENT_USER)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getCurrentUser() != null
    }
    
    fun getCurrentUser(): String? {
        return prefs.getString(KEY_CURRENT_USER, null)
    }
    
    private fun setCurrentUser(username: String) {
        prefs.edit()
            .putString(KEY_CURRENT_USER, username)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }
    
    private fun userExists(username: String): Boolean {
        return userPrefs.contains("user_$username")
    }
    
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}