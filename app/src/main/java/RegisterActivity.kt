package com.example.menstruacionnavapp.ui.register

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.menstruacionnavapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.example.menstruacionnavapp.databinding.ActivityRegisterBinding


class RegisterActivity : AppCompatActivity() {

    // Referencias a los campos del formulario
    private lateinit var etName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button

    private val db = FirebaseFirestore.getInstance() // Instancia de Firestore
    private val auth = FirebaseAuth.getInstance() // Instancia de autenticación de Firebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar las vistas
        etName = findViewById(R.id.etName)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)

        // Lógica para registrar el usuario al hacer clic en el botón
        btnRegister.setOnClickListener {
            registerUser()
        }
    }

    // Función para registrar al usuario y guardar los datos en Firestore
    private fun registerUser() {
        val name = etName.text.toString()
        val username = etUsername.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        // Validación básica
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            // Mostrar un mensaje de error
            return
        }

        // Crear usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Guardar los datos del usuario en Firestore
                    val user = hashMapOf(
                        "name" to name,
                        "username" to username,
                        "email" to email,
                        "password" to password // Guarda la contraseña solo si es necesario
                    )

                    val userRef = db.collection("usuarios").document(auth.currentUser!!.uid)
                    userRef.set(user)
                        .addOnSuccessListener {
                            // El usuario se registró con éxito
                            // Redirigir al usuario a la siguiente pantalla (por ejemplo, la pantalla principal)
                        }
                        .addOnFailureListener {
                            // Manejar el error si no se pudo guardar en Firestore
                        }
                } else {
                    // Manejar el error si no se pudo crear el usuario
                }
            }
    }
}
