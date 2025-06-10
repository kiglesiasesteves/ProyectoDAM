// PeriodActiveFragment.kt (Período activo)
package com.example.menstruacionnavapp.ui.calendario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.menstruacionnavapp.databinding.FragmentPeriodActiveBinding

class PeriodActiveFragment : Fragment() {

    private var _binding: FragmentPeriodActiveBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPeriodActiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun updatePeriodInfo(days: Int) {
        // Actualizar la imagen de la gota y la duración según el periodo
        binding.periodIcon.setImageResource(resources.getIdentifier("gota", "drawable", requireContext().packageName))
        binding.periodDays.setImageResource(resources.getIdentifier("number_$days", "drawable", requireContext().packageName))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
