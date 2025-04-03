package com.example.menstruacionnavapp.ui.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.menstruacionnavapp.MainActivity
import com.example.menstruacionnavapp.databinding.ActivityRegisterBinding
import com.example.menstruacionnavapp.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val name = binding.etName.text.toString()
            val username = binding.etUsername.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && username.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userId = user?.uid
                            val userData = hashMapOf(
                                "nombre" to name,
                                "nombreUsuario" to username,
                                "email" to email,
                                "contrasenha" to password
                            )

                            if (userId != null) {
                                db.collection("usuarios").document(userId).set(userData)
                                    .addOnSuccessListener {
                                        user?.sendEmailVerification()
                                            ?.addOnCompleteListener { verificationTask ->
                                                if (verificationTask.isSuccessful) {
                                                    Toast.makeText(
                                                        this,
                                                        "Correo de verificación enviado.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        this,
                                                        "Error al enviar el correo de verificación.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }

                                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                                        val intent = Intent(this, LoginActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { firestoreException ->
                                        Toast.makeText(
                                            this,
                                            "Error al guardar los datos: ${firestoreException.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            val exception = task.exception
                            Toast.makeText(
                                this,
                                "Error en el registro: ${exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d("ErrorRegistro", "Error en el registro: ${exception?.message}")
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}