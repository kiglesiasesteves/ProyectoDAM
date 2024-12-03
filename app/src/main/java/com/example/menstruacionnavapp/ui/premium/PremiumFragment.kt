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

class PremiumFragment : Fragment() {
    private var _binding: FragmentPremiumBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val premiumViewModel = ViewModelProvider(this).get(PremiumViewModel::class.java)

        _binding = FragmentPremiumBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textPremium
        val button: Button = binding.buttonHome


        button.setOnClickListener {
            premiumViewModel.onButtonClick()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}