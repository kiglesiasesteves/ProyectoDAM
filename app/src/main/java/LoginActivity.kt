package com

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.menstruacionnavapp.AppActivity
import com.example.menstruacionnavapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user?.isEmailVerified == true) {
                                val intent = Intent(this, AppActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Por favor, verifica tu correo electrónico antes de acceder.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            val exception = task.exception
                            Toast.makeText(
                                this,
                                "Error en el inicio de sesión: ${exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d("ErrorLogin", "Error en el inicio de sesión: ${exception?.message}")
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}