package com.example.menstruacionnavapp.ui.register.com.example.menstruacionnavapp.ui.GenerarEntrenamientos

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.menstruacionnavapp.databinding.FragmentFitnessBinding
import com.example.menstruacionnavapp.ui.register.com.example.menstruacionnavapp.model.EntornoEntrenamiento
import com.example.menstruacionnavapp.ui.register.com.example.menstruacionnavapp.model.FaseCiclo

class EntrenamientoFragment : Fragment() {

    private var _binding: FragmentFitnessBinding? = null
    private val binding get() = _binding!!
    private lateinit var trainingViewModel: TrainingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFitnessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trainingViewModel = ViewModelProvider(this).get(TrainingViewModel::class.java)

        trainingViewModel.entrenamiento.observe(viewLifecycleOwner, { entrenamiento ->
            actualizarUI(entrenamiento)
        })

        val faseActual = obtenerFaseActual()
        trainingViewModel.generarEntrenamiento(faseActual)
    }

    private fun actualizarUI(entrenamiento: EntornoEntrenamiento) {
        binding.tituloEjercicio.text = entrenamiento.ejercicio
        binding.descripcionEjercicio.text = entrenamiento.descripcion

        if (entrenamiento.videoUrl.isNotEmpty()) {
            val videoUri = Uri.parse(entrenamiento.videoUrl)
            binding.videoView.setVideoURI(videoUri)
            binding.videoView.start()
        }
    }

    private fun obtenerFaseActual(): FaseCiclo {
        return FaseCiclo.OVULATORIA
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}