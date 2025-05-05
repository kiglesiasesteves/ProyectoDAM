package com.example.menstruacionnavapp.ui.questionnaire

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.menstruacionnavapp.MainActivity
import com.example.menstruacionnavapp.databinding.ActivityQuestionnaireBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*

class QuestionnaireActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionnaireBinding
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionnaireBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el DatePickerDialog para etLastPeriod
        binding.etLastPeriod.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    binding.etLastPeriod.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.maxDate = calendar.timeInMillis
            datePicker.show()
        }

        binding.btnSubmit.setOnClickListener {
            val lastPeriod = binding.etLastPeriod.text.toString()
            val periodDuration = binding.etPeriodDuration.text.toString()

            if (lastPeriod.isNotEmpty() && periodDuration.isNotEmpty()) {
                val userId = auth.currentUser?.uid
                val questionnaireData = hashMapOf(
                    "ultimoPeriodo" to lastPeriod,
                    "duracionPeriodo" to periodDuration
                )

                if (userId != null) {
                    db.collection("usuarios").document(userId).set(questionnaireData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Error al guardar los datos: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}