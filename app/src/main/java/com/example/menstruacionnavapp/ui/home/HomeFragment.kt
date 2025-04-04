package com.example.menstruacionnavapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.menstruacionnavapp.databinding.FragmentHomeBinding
import com.example.menstruacionnavapp.ui.register.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.menstruacionnavapp.controller.MenstrualCycleController
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val periodIcon: ImageView = binding.periodIcon
        val cycleTextView: TextView = binding.cycleTextView
        val daysLeftTextView: TextView = binding.daysLeftTextView

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            getPeriodData(userId, periodIcon, cycleTextView)
        }

        binding.buttonHome.setOnClickListener {
            // Cerrar sesión del usuario
            FirebaseAuth.getInstance().signOut()

            // Redirigir a RegisterActivity
            val intent = Intent(activity, RegisterActivity::class.java)
            startActivity(intent)
            activity?.finish() // Finalizar la actividad actual
        }

        return root
    }

    private fun getPeriodData(
        userId: String,
        periodIcon: ImageView,
        cycleTextView: TextView,
    ) {
        MenstrualCycleController(userId).obtenerFaseActual { fase ->
            fase?.let {
                updateUI(userId, it, periodIcon, cycleTextView)
            } ?: run {
                Log.d("HomeFragment", "No se pudo obtener la fase actual.")
            }
        }
    }

    private fun updateUI(
        userId: String,
        fase: String,
        periodIcon: ImageView,
        cycleTextView: TextView,
    ) {
        MenstrualCycleController(userId).obtenerFases { fases ->
            fases?.let {
                val hoy = Date()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val detallesFases = it.entries.joinToString("\n") { (nombreFase, fechaInicio) ->
                    val diasRestantes = ((fechaInicio.time - hoy.time) / (1000 * 60 * 60 * 24)).toInt()
                    "$nombreFase: empieza en $diasRestantes días (el ${dateFormat.format(fechaInicio)})"
                }

                cycleTextView.text = "Fase actual: $fase\n\nDetalles de las fases:\n$detallesFases"
                periodIcon.visibility = View.VISIBLE
            } ?: run {
                Log.d("HomeFragment", "No se pudieron obtener las fases.")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}