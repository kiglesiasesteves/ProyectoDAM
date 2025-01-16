package com.example.menstruacionnavapp.ui.buscarGimnasios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.menstruacionnavapp.R
import com.example.menstruacionnavapp.databinding.FragmentGymsBinding
import com.google.android.gms.maps.SupportMapFragment

class GymFragment : Fragment() {
    private var _binding: FragmentGymsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val gymViewModel = ViewModelProvider(this).get(GymViewModel::class.java)

        _binding = FragmentGymsBinding.inflate(inflater, container, false)
        val root: View = binding.root



        // Initialize the SupportMapFragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            // Do something with the googleMap
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}