package com.example.menstruacionnavapp.ui.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.menstruacionnavapp.MainActivity
import com.example.menstruacionnavapp.databinding.ActivityRegisterBinding
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

        // Función de registro
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val name = binding.etName.text.toString()  // Nombre del usuario
            val username = binding.etUsername.text.toString()  // Nombre de usuario (nuevo campo)

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && username.isNotEmpty()) {
                // Crear usuario en Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userId = user?.uid
                            val userData = hashMapOf(
                                "nombre" to name,
                                "nombreUsuario" to username,  // Guardar nombre de usuario
                                "email" to email,
                                "contrasenha" to password  // Guardar la contraseña (aunque normalmente se recomienda no guardar la contraseña en texto plano)
                            )

                            if (userId != null) {
                                db.collection("usuarios").document(userId).set(userData)
                                    .addOnSuccessListener {
                                        // Enviar correo de verificación
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
                                        val intent = Intent(this, MainActivity::class.java)
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
                            // Si falla el registro en Firebase Authentication, obtenemos más detalles
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
                // Si algún campo está vacío
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
