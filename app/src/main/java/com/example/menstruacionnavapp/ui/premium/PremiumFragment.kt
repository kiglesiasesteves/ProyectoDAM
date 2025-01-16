package com.example.menstruacionnavapp.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.menstruacionnavapp.databinding.FragmentPremiumBinding
import com.example.menstruacionnavapp.ui.premium.PremiumViewModel
//fUNCIÓN pREMIUM paypal
class PremiumFragment : Fragment() {
    private var _binding: FragmentPremiumBinding? = null

    // Esta propiedad solo es válida entre onCreateView y onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Obtener el ViewModel para el fragmento
        val premiumViewModel = ViewModelProvider(this).get(PremiumViewModel::class.java)

        // Inflar el layout usando view binding
        _binding = FragmentPremiumBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Obtener referencias a los elementos de la vista
        val textView: TextView = binding.textPremium
        val button: Button = binding.buttonHome

        // Configurar el listener de clic para el botón
        button.setOnClickListener {
            premiumViewModel.onButtonClick()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiar el binding cuando la vista es destruida
        _binding = null
    }
}