package com.example.menstruacionnavapp.ui.premium

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.menstruacionnavapp.databinding.FragmentPremiumBinding
import com.example.menstruacionnavapp.util.PayPalHelper

class PremiumFragment : Fragment() {
    private var _binding: FragmentPremiumBinding? = null
    private val binding get() = _binding!!
    private lateinit var premiumViewModel: PremiumViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Obtener el ViewModel para el fragmento
        premiumViewModel = ViewModelProvider(this).get(PremiumViewModel::class.java)

        // Inflar el layout usando view binding
        _binding = FragmentPremiumBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Iniciar el servicio de PayPal
        activity?.let { PayPalHelper.startPayPalService(it) }

        // Configurar el listener para el botón de pago
        binding.buttonHome.setOnClickListener {
            activity?.let { 
                premiumViewModel.processPayment(it, 3.0) // 3€ para la suscripción
            }
        }

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        val success = premiumViewModel.onActivityResult(requestCode, resultCode, data)
        if (success) {
            Toast.makeText(context, "¡Pago completado con éxito!", Toast.LENGTH_LONG).show()
            // Aquí puedes actualizar el estado premium del usuario
        } else {
            Toast.makeText(context, "El pago no se ha completado", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detener el servicio de PayPal al destruir el fragmento
        activity?.let { PayPalHelper.stopPayPalService(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiar el binding cuando la vista es destruida
        _binding = null
    }
}
