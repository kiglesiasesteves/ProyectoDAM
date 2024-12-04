package com.example.menstruacionnavapp.ui.premium

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.menstruacionnavapp.databinding.FragmentPremiumBinding

class PremiumFragment : Fragment() {
    private var _binding: FragmentPremiumBinding? = null
    private val binding get() = _binding!!
    private lateinit var premiumViewModel: PremiumViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        premiumViewModel = ViewModelProvider(this).get(PremiumViewModel::class.java)

        _binding = FragmentPremiumBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val button: Button = binding.buttonHome
        button.setOnClickListener {
            premiumViewModel.onButtonClick(requireActivity())
        }

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        premiumViewModel.handleActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}