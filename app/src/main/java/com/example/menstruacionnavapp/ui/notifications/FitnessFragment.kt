package com.example.menstruacionnavapp.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.menstruacionnavapp.R
import com.example.menstruacionnavapp.databinding.FragmentFitnessBinding

class FitnessFragment : Fragment() {

    // View binding para el fragmento
    private var _binding: FragmentFitnessBinding? = null
    private val binding get() = _binding!!

    // Inflar el layout para este fragmento
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFitnessBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Configurar la vista y los listeners de clic
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar el listener de clic para el botón que navega a GymsFragment
        binding.buttonSearchGyms.setOnClickListener { // Botón para buscar gimnasios
            findNavController().navigate(R.id.action_fitnessFragment_to_gymsFragment)
        }
    }

    // Limpiar el binding cuando la vista es destruida
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}