package com.example.menstruacionnavapp.ui.buscarGimnasios

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.menstruacionnavapp.R
import com.example.menstruacionnavapp.databinding.FragmentGymsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class GymFragment : Fragment() {
    private var _binding: FragmentGymsBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val gymViewModel = ViewModelProvider(this).get(GymViewModel::class.java)
        _binding = FragmentGymsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize FusedLocationProviderClient to get device location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize the SupportMapFragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback { googleMap ->
            this.googleMap = googleMap
            // Enable the My Location button if permission is granted
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                googleMap.isMyLocationEnabled = true
            } else {
                // Handle permission denial
            }

            // Get the current location of the device
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }

            // Add markers (for demonstration purposes)
            val gymLocation = LatLng(42.22560424737218, -8.73470873234326) // Example coordinates (replace with actual gym locations)
            googleMap.addMarker(MarkerOptions().position(gymLocation).title("Synergym Torrecedeira"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(gymLocation))
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
