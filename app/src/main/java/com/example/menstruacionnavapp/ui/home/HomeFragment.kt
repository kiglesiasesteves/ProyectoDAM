package com.example.menstruacionnavapp.ui.home

import Usuario
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
import com.google.firebase.firestore.FirebaseFirestore
import com.example.menstruacionnavapp.R
import com.google.firebase.Timestamp
import java.util.*
import kotlin.math.roundToInt

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
        val anotherIcon: ImageView = binding.anotherIcon
        val cycleTextView: TextView = binding.cycleTextView
        val daysLeftTextView: TextView = binding.daysLeftTextView

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            getPeriodData(userId, periodIcon, daysIcon, anotherIcon, cycleTextView, daysLeftTextView)
        }

        binding.buttonHome.setOnClickListener {
            // Sign out the user
            FirebaseAuth.getInstance().signOut()

            // Redirect to RegisterActivity
            val intent = Intent(activity, RegisterActivity::class.java)
            startActivity(intent)
            activity?.finish() // Finish the current activity
        }

        return root
    }

    private fun getPeriodData(
        userId: String,
        periodIcon: ImageView,
        daysIcon: ImageView,
        anotherIcon: ImageView,
        cycleTextView: TextView,
        daysLeftTextView: TextView
    ) {
        db.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val periodos = document.get("periodos") as? List<Timestamp> ?: emptyList()
                    val finPeriodos = document.get("finPeriodos") as? List<Timestamp> ?: emptyList()

                    if (periodos.isNotEmpty() && finPeriodos.isNotEmpty()) {
                        val usuario = calcularUsuario(periodos.map { it.toDate() }, finPeriodos.map { it.toDate() })
                        updateUI(usuario, periodIcon, daysIcon, anotherIcon, cycleTextView, daysLeftTextView)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.d("HomeFragment", "Error fetching document: ", e)
            }
    }

    private fun calcularUsuario(periodos: List<Date>, finPeriodos: List<Date>): Usuario {
        val ciclos = mutableListOf<Int>()
        val sangrados = mutableListOf<Int>()

        for (i in 1 until periodos.size) {
            val diasCiclo = ((periodos[i].time - periodos[i - 1].time) / (1000 * 60 * 60 * 24)).toInt()
            ciclos.add(diasCiclo)
        }

        for (i in finPeriodos.indices) {
            val diasSangrado = ((finPeriodos[i].time - periodos[i].time) / (1000 * 60 * 60 * 24)).toInt()
            sangrados.add(diasSangrado)
        }

        val promedioCiclo = if (ciclos.isNotEmpty()) ciclos.average().roundToInt() else 28
        val promedioSangrado = if (sangrados.isNotEmpty()) sangrados.average().roundToInt() else 5

        val ultimoInicio = periodos.last()
        val proximoInicioEstimado = Calendar.getInstance().apply {
            time = ultimoInicio
            add(Calendar.DAY_OF_MONTH, promedioCiclo)
        }.time

        val diasParaSiguiente = ((proximoInicioEstimado.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()

        return Usuario(promedioCiclo, diasParaSiguiente, promedioSangrado, periodos, finPeriodos)
    }

    private fun updateUI(
        usuario: Usuario,
        periodIcon: ImageView,
        daysIcon: ImageView,
        anotherIcon: ImageView,
        cycleTextView: TextView,
        daysLeftTextView: TextView
    ) {
        cycleTextView.text = "Duración media del ciclo: ${usuario.promedioCiclo} días"
        daysLeftTextView.text = "Días hasta el próximo ciclo: ${usuario.diasParaSiguiente}"

        if (usuario.diasParaSiguiente in 1..5) {
            val imageName = listOf("uno", "dos", "tres", "cuatro", "cinco")[usuario.diasParaSiguiente - 1]
            val imageResId = resources.getIdentifier(imageName, "drawable", requireContext().packageName)
            periodIcon.setImageResource(imageResId)
        } else {
            periodIcon.setImageResource(R.drawable.cero)
        }

        periodIcon.visibility = View.VISIBLE
        daysIcon.visibility = View.VISIBLE
        anotherIcon.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}