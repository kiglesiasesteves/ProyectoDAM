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
import java.util.*

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var selectedDate: Long = System.currentTimeMillis()
    private var duration: Int = 5 // Duración por defecto

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkIfInPeriod()

        binding.btnRegisterPeriod.setOnClickListener {
            registerPeriod()
        }
    }

    private fun registerPeriod() {
        val userId = auth.currentUser?.uid ?: return

        if (selectedDate > System.currentTimeMillis()) {
            Toast.makeText(requireContext(), "No puedes registrar un período en el futuro", Toast.LENGTH_SHORT).show()
            return
        }

        val timestamp = Timestamp(Date(selectedDate))
        val userRef = db.collection("usuarios").document(userId)
        val periodData = hashMapOf(
            "inicio" to timestamp,
            "duracion" to duration
        )

        userRef.update("periodos", FieldValue.arrayUnion(periodData))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Período registrado", Toast.LENGTH_SHORT).show()
                checkIfInPeriod()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfInPeriod() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("usuarios").document(userId)
        val today = System.currentTimeMillis()

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val periodos = document.get("periodos") as? List<Map<String, Any>> ?: emptyList()

                val activePeriod = periodos.find { periodo ->
                    val inicio = (periodo["inicio"] as Timestamp).toDate().time
                    val duracion = (periodo["duracion"] as Long).toInt()
                    val fin = inicio + (duracion * 24 * 60 * 60 * 1000)
                    today in inicio..fin
                }

                if (activePeriod != null) {
                    val inicio = (activePeriod["inicio"] as Timestamp).toDate().time
                    val daysInPeriod = ((today - inicio) / (24 * 60 * 60 * 1000)).toInt() + 1
                    showPeriodInfo(daysInPeriod)
                } else {
                    showNoPeriod()
                }
            }
        }
    }

    private fun showPeriodInfo(days: Int) {
        // Muestra el icono de la gota y la duración en base a las imágenes de los días
        binding.periodInfoContainer.visibility = View.VISIBLE
        binding.periodIcon.setImageResource(resources.getIdentifier("gota", "drawable", requireContext().packageName))
        binding.periodDays.setImageResource(resources.getIdentifier("$days", "drawable", requireContext().packageName))
        binding.btnRegisterPeriod.isEnabled = false // Desactiva el botón
    }

    private fun showNoPeriod() {
        // Si no hay período, se oculta la sección de información y se activa el botón
        binding.periodInfoContainer.visibility = View.GONE
        binding.btnRegisterPeriod.isEnabled = true // Activa el botón
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}