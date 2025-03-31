package com.example.menstruacionnavapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.menstruacionnavapp.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.menstruacionnavapp.R
import com.google.firebase.Timestamp
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val periodIcon: ImageView = binding.periodIcon
        val daysIcon: ImageView = binding.daysIcon

        // Obtener el usuario actual
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            getPeriodData(userId, periodIcon, daysIcon)
        }

        return root
    }

    private fun getPeriodData(userId: String, periodIcon: ImageView, daysIcon: ImageView) {
        Log.d("HomeFragment", "Fetching period data for user: $userId")
        db.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("HomeFragment", "Document exists for user: $userId")
                    val periodos = document.get("periodos") as? List<Timestamp> ?: emptyList()
                    val finPeriodos = document.get("finPeriodos") as? List<Timestamp> ?: emptyList()
                    Log.d("HomeFragment", "Periodos: $periodos")
                    Log.d("HomeFragment", "FinPeriodos: $finPeriodos")

                    if (periodos.isNotEmpty()) {
                        val lastStartPeriod = periodos.last() // Última fecha de inicio del período
                        Log.d("HomeFragment", "Last start period: $lastStartPeriod")

                        // Si hay menos elementos en finPeriodos que en Periodos, significa que el último período no ha finalizado
                        val periodoEnCurso = finPeriodos.size < periodos.size
                        Log.d("HomeFragment", "Periodo en curso: $periodoEnCurso")

                        val diasEnCurso = if (periodoEnCurso) {
                            // Si el período está en curso, calcular los días desde la última fecha de inicio hasta hoy
                            val inicioDate = lastStartPeriod.toDate()
                            val today = Calendar.getInstance().time
                            val days = ((today.time - inicioDate.time) / (1000 * 60 * 60 * 24)).toInt()
                            Log.d("HomeFragment", "Inicio date: $inicioDate")
                            Log.d("HomeFragment", "Today: $today")
                            Log.d("HomeFragment", "Days in period: $days")
                            days
                        } else {
                            Log.d("HomeFragment", "No active period")
                            0
                        }

                        // Actualizar la UI
                        updateUI(diasEnCurso, periodIcon, daysIcon)
                    } else {
                        Log.d("HomeFragment", "No periods found")
                        updateUI(0, periodIcon, daysIcon)
                    }
                } else {
                    Log.d("HomeFragment", "Document does not exist for user: $userId")
                }
            }
            .addOnFailureListener { e ->
                Log.d("HomeFragment", "Error fetching document: ", e)
            }
    }

    private fun updateUI(dias: Int, periodIcon: ImageView, daysIcon: ImageView) {
        if (dias > 0) {
            val imageName = when (dias) {
                1 -> "uno"
                2 -> "dos"
                3 -> "tres"
                4 -> "cuatro"
                5 -> "cinco"
                else -> "cinco" // Si pasa de 5, usar cinco.png
            }
            val imageResId = resources.getIdentifier(imageName, "drawable", requireContext().packageName)
            periodIcon.setImageResource(imageResId)
            periodIcon.visibility = View.VISIBLE
            daysIcon.visibility = View.VISIBLE
        } else {
            periodIcon.setImageResource(R.drawable.cero)
            periodIcon.visibility = View.VISIBLE
            daysIcon.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}