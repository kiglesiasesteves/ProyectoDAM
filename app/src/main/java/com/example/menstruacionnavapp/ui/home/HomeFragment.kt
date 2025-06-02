package com.example.menstruacionnavapp.ui.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.menstruacionnavapp.databinding.FragmentHomeBinding
import com.example.menstruacionnavapp.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.menstruacionnavapp.controller.GenerarInforme
import com.example.menstruacionnavapp.controller.MenstrualCycleController
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var userId: String? = null
    private lateinit var savePdfLauncher: ActivityResultLauncher<Intent>
    private var informeContenido: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Registrar el lanzador para guardar el archivo
        savePdfLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    guardarPdf(uri, informeContenido.orEmpty())
                    abrirPdf(uri)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val periodIcon: ImageView = binding.periodIcon
        val cycleTextView: TextView = binding.cycleTextView
        val buttonGenerateReport: Button = binding.buttonGenerateReport

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            userId = it.uid
            getPeriodData(it.uid, periodIcon, cycleTextView)

            buttonGenerateReport.setOnClickListener {
                generarInforme(userId!!)
            }
        }

        binding.buttonHome.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return root
    }

    private fun getPeriodData(
        userId: String,
        periodIcon: ImageView,
        cycleTextView: TextView
    ) {
        MenstrualCycleController(userId).obtenerFaseActual { fase -> 
            fase?.let {
                updateUI(userId, it, periodIcon, cycleTextView)
            } ?: Log.d("HomeFragment", "No se pudo obtener la fase actual.")
        }
    }

    private fun updateUI(
        userId: String,
        fase: String,
        periodIcon: ImageView,
        cycleTextView: TextView
    ) {
        MenstrualCycleController(userId).obtenerFases { fases -> 
            fases?.let {
                val hoy = Date()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val detallesFases = it.entries.joinToString("\n") { (nombreFase, fechaInicio) -> 
                    val diasRestantes = max(0, ((fechaInicio.time - hoy.time) / (1000 * 60 * 60 * 24)).toInt())

                    if (fechaInicio.time <= hoy.time) {
                        "$nombreFase: fase actual (comenzó el ${dateFormat.format(fechaInicio)})"
                    } else {
                        "$nombreFase: empieza en $diasRestantes días (el ${dateFormat.format(fechaInicio)})"
                    }
                }

                cycleTextView.text = "Fase actual: $fase\n\nDetalles de las fases:\n$detallesFases"
                periodIcon.visibility = View.VISIBLE
            } ?: Log.d("HomeFragment", "No se pudieron obtener las fases.")
        }
    }

    private fun generarInforme(userId: String) {
        val context = requireContext()
        val generarInforme = GenerarInforme(userId, context)
        generarInforme.generarInforme { informe -> 
            Log.d("HomeFragment", "Informe generado:\n$informe")
            this.informeContenido = informe

            // Lanzar el selector para guardar el PDF
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
                putExtra(Intent.EXTRA_TITLE, "InformeCicloMenstrual.pdf")
            }
            savePdfLauncher.launch(intent)
        }
    }

    private fun guardarPdf(uri: Uri, contenido: String) {
        try {
            val outputStream: OutputStream? = requireContext().contentResolver.openOutputStream(uri)
            outputStream?.bufferedWriter()?.use {
                it.write(contenido)
            }
            Toast.makeText(requireContext(), "Informe guardado correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al guardar el PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun abrirPdf(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("HomeFragment", "No se pudo abrir el PDF. Asegúrate de tener una app compatible", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
