package com.example.menstruacionnavapp.ui.calendario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.menstruacionnavapp.databinding.FragmentCalendarBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance() // Instancia de Firestore
    private val auth = FirebaseAuth.getInstance() // Instancia de Firebase Auth

    private var selectedDate: Long = System.currentTimeMillis() // Fecha por defecto (hoy)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val today = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = today
        calendar.add(Calendar.MONTH, -6) // Restar 6 meses
        val sixMonthsAgo = calendar.timeInMillis

        // Capturar la fecha seleccionada en el CalendarView
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            selectedDate = selectedCalendar.timeInMillis
        }

        // Acción cuando se presiona el botón "Registrar nuevo período"
        binding.btnRegisterPeriod.setOnClickListener {
            val userId = auth.currentUser?.uid

            if (userId == null) {
                Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDate > today) {
                Toast.makeText(requireContext(), "No puedes registrar un período en el futuro", Toast.LENGTH_SHORT).show()
            } else if (selectedDate < sixMonthsAgo) {
                Toast.makeText(requireContext(), "Solo puedes registrar períodos de los últimos 6 meses", Toast.LENGTH_SHORT).show()
            } else {
                val timestamp = Timestamp(Date(selectedDate))

                val userRef = db.collection("usuarios").document(userId)

                userRef.update("periodos", FieldValue.arrayUnion(timestamp))
                    .addOnSuccessListener {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val formattedDate = dateFormat.format(Date(selectedDate))
                        Toast.makeText(requireContext(), "Período registrado: $formattedDate", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error al registrar el período: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
