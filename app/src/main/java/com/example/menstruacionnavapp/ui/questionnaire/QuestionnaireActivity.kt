package com.example.menstruacionnavapp.ui.questionnaire

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.menstruacionnavapp.AppActivity
import com.example.menstruacionnavapp.databinding.ActivityQuestionnaireBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*
import android.text.Editable
import android.text.TextWatcher
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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

        // Formatear automáticamente la fecha de nacimiento
        binding.etBirthDate.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val divider = "/"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Evita procesamiento recursivo
                if (isUpdating) return

                val inputText = s.toString().replace(divider, "")

                // Si no hay cambios, no hacemos nada
                if (inputText.isEmpty()) return

                val formattedText = StringBuilder()
                var cursorPosition = binding.etBirthDate.selectionStart

                isUpdating = true

                // Máximo 8 caracteres (ddmmaaaa)
                val cleanText = inputText.take(8)

                for (i in cleanText.indices) {
                    formattedText.append(cleanText[i])

                    // Añadir separador después de día (2 dígitos) y mes (4 dígitos en total)
                    if (i == 1 || i == 3) {
                        if (i < cleanText.length - 1) {
                            formattedText.append(divider)
                            // Ajustar cursor si está después del separador
                            if (cursorPosition > i) cursorPosition++
                        } else if (count != 0) { // Solo añadir automáticamente el separador cuando se añade un dígito
                            formattedText.append(divider)
                            cursorPosition++
                        }
                    }
                }

                binding.etBirthDate.setText(formattedText.toString())
                // Asegurar que el cursor esté en posición válida
                binding.etBirthDate.setSelection(cursorPosition.coerceAtMost(formattedText.length))

                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSubmit.setOnClickListener {
            val birthDate = binding.etBirthDate.text.toString()
            val lastPeriod = binding.etLastPeriod.text.toString()
            val periodDuration = binding.etPeriodDuration.text.toString()
            val isPregnancyChecked = binding.cbPregnancy.isChecked
            val isSportChecked = binding.cbSport.isChecked
            val isPredictionChecked = binding.cbPrediction.isChecked

            val missingFields = mutableListOf<String>()

            if (birthDate.isEmpty()) {
                binding.etBirthDate.error = "Campo obligatorio"
                missingFields.add("Fecha de nacimiento")
            }

            if (lastPeriod.isEmpty()) {
                binding.etLastPeriod.error = "Campo obligatorio"
                missingFields.add("Último periodo")
            }

            if (periodDuration.isEmpty()) {
                binding.etPeriodDuration.error = "Campo obligatorio"
                missingFields.add("Duración del periodo")
            }

            if (!isPregnancyChecked && !isSportChecked && !isPredictionChecked) {
                Toast.makeText(this, "Selecciona al menos una opción en '¿Para qué quieres usar la aplicación?'", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (missingFields.isNotEmpty()) {
                Toast.makeText(this, "Por favor, completa los campos obligatorios.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val purposes = mutableListOf<String>()
            if (isPregnancyChecked) purposes.add("Embarazo")
            if (isSportChecked) purposes.add("Deporte")
            if (isPredictionChecked) purposes.add("Previsión")

            val age = calculateAge(birthDate)
            if (age != null) {
                val userId = auth.currentUser?.uid
                val questionnaireData = hashMapOf(
                    "fechaNacimiento" to birthDate,
                    "edad" to age,
                    "ultimoPeriodo" to lastPeriod,
                    "duracionPeriodo" to periodDuration,
                    "propositos" to purposes
                )

                if (userId != null) {
                    db.collection("usuarios").document(userId).set(questionnaireData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
                            // Redireccionar a AppActivity después de guardar el cuestionario
                            val intent = Intent(this, AppActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Error al guardar los datos: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Por favor, ingresa una fecha de nacimiento válida.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateAge(birthDate: String): Int? {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val date = LocalDate.parse(birthDate, formatter)
            val today = LocalDate.now()
            Period.between(date, today).years
        } catch (e: DateTimeParseException) {
            null
        }
    }
}
