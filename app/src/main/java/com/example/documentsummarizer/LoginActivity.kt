package com.example.documentsummarizer

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    
    private lateinit var authManager: AuthManager
    private var isLoginMode = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            authManager = AuthManager(this)
            
            // Check if user is already logged in
            if (authManager.isLoggedIn()) {
                navigateToMain()
                return
            }
            
            setContentView(R.layout.activity_login)
            setupViews()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
    
    private fun setupViews() {
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnAuth = findViewById<Button>(R.id.btnAuth)
        val btnToggleMode = findViewById<Button>(R.id.btnToggleMode)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        
        updateUI(tvTitle, etConfirmPassword, btnAuth, btnToggleMode)
        
        btnAuth.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            
            if (validateInput(username, password, confirmPassword)) {
                if (isLoginMode) {
                    performLogin(username, password)
                } else {
                    performSignup(username, password)
                }
            }
        }
        
        btnToggleMode.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUI(tvTitle, etConfirmPassword, btnAuth, btnToggleMode)
        }
    }
    
    private fun updateUI(tvTitle: TextView, etConfirmPassword: EditText, btnAuth: Button, btnToggleMode: Button) {
        if (isLoginMode) {
            tvTitle.text = "Login"
            etConfirmPassword.visibility = android.view.View.GONE
            btnAuth.text = "Login"
            btnToggleMode.text = "Need an account? Sign up"
        } else {
            tvTitle.text = "Sign Up"
            etConfirmPassword.visibility = android.view.View.VISIBLE
            btnAuth.text = "Sign Up"
            btnToggleMode.text = "Have an account? Login"
        }
    }
    
    private fun validateInput(username: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty()) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (password.isEmpty()) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (!isLoginMode && password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    private fun performLogin(username: String, password: String) {
        if (authManager.login(username, password)) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            navigateToMain()
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun performSignup(username: String, password: String) {
        if (authManager.signup(username, password)) {
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
            navigateToMain()
        } else {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}