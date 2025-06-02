package com.example.menstruacionnavapp.ui.calendario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.menstruacionnavapp.R
import com.example.menstruacionnavapp.databinding.FragmentCalendarBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
//CalendarioFRagment
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedDate: Long = System.currentTimeMillis()
    private var lastRegisteredPeriodStart: Timestamp? = null

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
        calendar.add(Calendar.MONTH, -6)
        val sixMonthsAgo = calendar.timeInMillis

        // Limitar la fecha máxima seleccionable en el calendario a hoy
        binding.calendarView.maxDate = today

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            selectedDate = selectedCalendar.timeInMillis
            updateButtonStates()
        }

        binding.btnRegisterPeriod.setOnClickListener {
            val userId = auth.currentUser?.uid ?: run {
                Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when {
                selectedDate > System.currentTimeMillis() -> {
                    Toast.makeText(requireContext(), "No puedes registrar un período en el futuro", Toast.LENGTH_SHORT).show()
                }
                selectedDate < sixMonthsAgo -> {
                    Toast.makeText(requireContext(), "Solo puedes registrar períodos de los últimos 6 meses", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val newPeriodStart = Timestamp(Date(selectedDate))

                    // Primero verificar que no se solape con períodos existentes
                    db.collection("usuarios").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val periodos = document.get("periodos") as? List<Timestamp> ?: emptyList()
                                val finPeriodos = document.get("finPeriodos") as? List<Timestamp> ?: emptyList()

                                // Verificar solapamiento
                                if (hasOverlappingPeriod(newPeriodStart, periodos, finPeriodos)) {
                                    Toast.makeText(requireContext(), "Error: Este período se solapa con otro existente", Toast.LENGTH_LONG).show()
                                } else {
                                    // Registrar el nuevo período
                                    db.collection("usuarios").document(userId)
                                        .update("periodos", FieldValue.arrayUnion(newPeriodStart))
                                        .addOnSuccessListener {
                                            showDateToast("Período registrado")
                                            lastRegisteredPeriodStart = newPeriodStart
                                            checkIfInPeriod()
                                            updateButtonStates()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(requireContext(), "Error al registrar: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Error al verificar períodos", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        binding.btnRegisterEndPeriod.setOnClickListener {
            val userId = auth.currentUser?.uid ?: run {
                Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificar que la fecha de fin no sea anterior a la de inicio
            if (lastRegisteredPeriodStart != null && selectedDate < lastRegisteredPeriodStart!!.toDate().time) {
                Toast.makeText(requireContext(), "La fecha de fin no puede ser anterior a la fecha de inicio del período", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val timestamp = Timestamp(Date(selectedDate))
            db.collection("usuarios").document(userId)
                .update("finPeriodos", FieldValue.arrayUnion(timestamp))
                .addOnSuccessListener {
                    showDateToast("Fin de período registrado")
                    lastRegisteredPeriodStart = null
                    checkIfInPeriod()
                    updateButtonStates()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al registrar fin: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        loadLastPeriodStart()
        checkIfInPeriod()
    }

    // Cargar el último inicio de periodo registrado
    private fun loadLastPeriodStart() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val periodos = document.get("periodos") as? List<Timestamp> ?: emptyList()
                    val finPeriodos = document.get("finPeriodos") as? List<Timestamp> ?: emptyList()

                    if (periodos.size > finPeriodos.size) {
                        lastRegisteredPeriodStart = periodos.last()
                    }

                    updateButtonStates()
                }
            }
    }

    // Actualizar el estado de habilitación de los botones
    private fun updateButtonStates() {
        val isInPeriod = lastRegisteredPeriodStart != null
        binding.btnRegisterPeriod.isEnabled = !isInPeriod
        binding.btnRegisterEndPeriod.isEnabled = isInPeriod
    }

    private fun showDateToast(messagePrefix: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(selectedDate))
        Toast.makeText(requireContext(), "$messagePrefix: $formattedDate", Toast.LENGTH_SHORT).show()
    }

    private fun checkIfInPeriod() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val periodos = document.get("periodos") as? List<Timestamp> ?: emptyList()
                    val finPeriodos = document.get("finPeriodos") as? List<Timestamp> ?: emptyList()

                    // Sort both lists chronologically
                    val sortedStarts = periodos.sortedBy { it.seconds }
                    val sortedEnds = finPeriodos.sortedBy { it.seconds }

                    val today = Timestamp.now()
                    var isInPeriod = false
                    var daysInPeriod = 0

                    // Check if we have more starts than ends (meaning current period is ongoing)
                    if (sortedStarts.size > sortedEnds.size) {
                        val lastStart = sortedStarts.last()
                        lastRegisteredPeriodStart = lastStart
                        if (today >= lastStart) {
                            isInPeriod = true
                            daysInPeriod = ((today.seconds - lastStart.seconds) / (24 * 60 * 60)).toInt() + 1
                        }
                    } else {
                        lastRegisteredPeriodStart = null
                    }

                    // Also check if today is between any start and end date
                    for (i in sortedEnds.indices) {
                        val start = sortedStarts.getOrNull(i) ?: continue
                        val end = sortedEnds[i]
                        if (today in start..end) {
                            isInPeriod = true
                            daysInPeriod = ((today.seconds - start.seconds) / (24 * 60 * 60)).toInt() + 1
                            break
                        }
                    }

                    if (isInPeriod) {
                        showPeriodInfo(daysInPeriod)
                    } else {
                        showNoPeriod()
                    }

                    // Update last period info
                    updateLastPeriodInfo(sortedStarts, sortedEnds, isInPeriod)
                    updateButtonStates()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al verificar período", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPeriodInfo(days: Int) {
        binding.periodInfoContainer.visibility = View.VISIBLE

        // Safer way to get drawable resources
        val dropIcon = ContextCompat.getDrawable(requireContext(), R.drawable.gota)
        binding.periodIcon.setImageDrawable(dropIcon)

        // Map days to available number drawables (1-10)
        val dayResource = when {
            days in 1..10 -> "number_$days"
            else -> "number_10" // default to 10 if more than 10
        }

        val resId = resources.getIdentifier(dayResource, "drawable", requireContext().packageName)
        if (resId != 0) {
            binding.periodDays.setImageResource(resId)
        }

        binding.btnRegisterPeriod.isEnabled = false
        binding.btnRegisterEndPeriod.isEnabled = true
    }

    private fun showNoPeriod() {
        binding.periodInfoContainer.visibility = View.GONE
        binding.btnRegisterPeriod.isEnabled = true
        binding.btnRegisterEndPeriod.isEnabled = false
    }

    private fun updateLastPeriodInfo(sortedStarts: List<Timestamp>, sortedEnds: List<Timestamp>, isInPeriod: Boolean) {
        if (sortedStarts.isNotEmpty()) {
            val lastStart = sortedStarts.last()
            val lastEnd = if (sortedEnds.size == sortedStarts.size) sortedEnds.last() else null

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val startDate = dateFormat.format(lastStart.toDate())
            val endDate = lastEnd?.let { dateFormat.format(it.toDate()) } ?: "hoy"

            binding.lastPeriodInfo.text = "Tu último período ha sido desde: $startDate hasta: $endDate"
            binding.lastPeriodInfo.visibility = View.VISIBLE
        } else {
            binding.lastPeriodInfo.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hasOverlappingPeriod(
        newStart: Timestamp,
        existingStarts: List<Timestamp>,
        existingEnds: List<Timestamp>
    ): Boolean {
        // Asumimos que los períodos están ordenados cronológicamente
        val sortedStarts = existingStarts.sortedBy { it.seconds }
        val sortedEnds = existingEnds.sortedBy { it.seconds }

        // Caso 1: Hay más inicios que fines (período actual sin terminar)
        if (sortedStarts.size > sortedEnds.size) {
            val lastStart = sortedStarts.last()
            // El nuevo inicio está dentro del período actual sin terminar
            if (newStart >= lastStart) {
                return true
            }
        }

        // Caso 2: Verificar con todos los períodos completos
        for (i in sortedEnds.indices) {
            val start = sortedStarts.getOrNull(i) ?: continue
            val end = sortedEnds[i]

            // El nuevo inicio está dentro de este período
            if (newStart in start..end) {
                return true
            }

            // El nuevo período incluye este período completo
            if (i < sortedStarts.size - 1) {
                val nextStart = sortedStarts[i + 1]
                if (newStart < end && nextStart > newStart) {
                    return true
                }
            }
        }

        return false
    }
}

