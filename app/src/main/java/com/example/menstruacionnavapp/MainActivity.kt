package com.example.menstruacionnavapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.menstruacionnavapp.databinding.ActivityMainBinding
import com.LoginActivity
import com.RegisterActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is logged in
        val user = auth.currentUser
        if (user != null && user.isEmailVerified) {
            // User is logged in and email is verified, redirect to AppActivity
            val intent = Intent(this, AppActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // User is not logged in, show the main screen
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.btnRegister.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                Log.d("MainActivity", "Register button clicked")
                startActivity(intent)
            }

            binding.btnLogin.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                Log.d("MainActivity", "Login button clicked")
                startActivity(intent)
            }
        }
    }
}