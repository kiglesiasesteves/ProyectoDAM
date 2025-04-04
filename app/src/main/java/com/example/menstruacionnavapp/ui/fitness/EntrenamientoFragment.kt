package com.example.menstruacionnavapp.ui.register.com.example.menstruacionnavapp.ui.GenerarEntrenamientos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.menstruacionnavapp.controller.MenstrualCycleController
import com.example.menstruacionnavapp.databinding.FragmentFitnessBinding
import com.example.menstruacionnavapp.ui.register.com.example.menstruacionnavapp.model.EntornoEntrenamiento
import com.example.menstruacionnavapp.ui.register.com.example.menstruacionnavapp.model.FaseCiclo
import com.google.firebase.auth.FirebaseAuth

class EntrenamientoFragment : Fragment() {

    private var _binding: FragmentFitnessBinding? = null
    private val binding get() = _binding!!
    private lateinit var trainingViewModel: TrainingViewModel
    private lateinit var menstrualCycleController: MenstrualCycleController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFitnessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trainingViewModel = ViewModelProvider(this)[TrainingViewModel::class.java]

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        menstrualCycleController = MenstrualCycleController(userId)

        menstrualCycleController.obtenerFaseActual { faseStr ->
            faseStr?.let {
                val faseActual = convertirAFaseCiclo(it)
                trainingViewModel.generarEntrenamiento(faseActual)
            }
        }

        trainingViewModel.entrenamiento.observe(viewLifecycleOwner) { entrenamiento ->
            actualizarUI(entrenamiento)
        }
    }

    private fun convertirAFaseCiclo(fase: String): FaseCiclo {
        return when (fase) {
            "Menstruación" -> FaseCiclo.MENSTRUAL
            "Fase Folicular" -> FaseCiclo.FOLICULAR
            "Ovulación" -> FaseCiclo.OVULATORIA
            "Fase Lútea" -> FaseCiclo.LUTEA
            else -> FaseCiclo.FOLICULAR
        }
    }

    private fun actualizarUI(entrenamiento: EntornoEntrenamiento) {
        binding.tituloEjercicio.text = entrenamiento.ejercicio
        binding.descripcionEjercicio.text = entrenamiento.descripcion

        if (entrenamiento.videoUrl.isNotEmpty()) {
            val videoId = obtenerIdYoutube(entrenamiento.videoUrl)
            val iframeHtml = """
                <html>
                <body style="margin:0;padding:0;">
                <iframe width="100%" height="100%" src="https://www.youtube.com/embed/$videoId" 
                    frameborder="0" allowfullscreen></iframe>
                </body>
                </html>
            """.trimIndent()

            binding.webView.settings.javaScriptEnabled = true
            binding.webView.settings.loadWithOverviewMode = true
            binding.webView.settings.useWideViewPort = true
            binding.webView.settings.pluginState = WebSettings.PluginState.ON
            binding.webView.loadData(iframeHtml, "text/html", "utf-8")
        }
    }

    private fun obtenerIdYoutube(url: String): String {
        return url.substringAfter("watch?v=").substringBefore("&")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
