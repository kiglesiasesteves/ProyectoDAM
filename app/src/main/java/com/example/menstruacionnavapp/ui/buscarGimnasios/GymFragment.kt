package com.example.menstruacionnavapp.ui.buscarGimnasios

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.menstruacionnavapp.R
import com.example.menstruacionnavapp.databinding.FragmentGymsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class GymFragment : Fragment() {
    private var _binding: FragmentGymsBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val gymViewModel = ViewModelProvider(this).get(GymViewModel::class.java)
        _binding = FragmentGymsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback { googleMap ->
            this.googleMap = googleMap
            checkLocationPermission()
            setupMapMarkers()
        })

        return root
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            enableUserLocation()
        }
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, null
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                }
            }

        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation()
            }
        }
    }

    private fun setupMapMarkers() {
        val boundsBuilder = LatLngBounds.builder()
        val gyms = listOf(
            Triple(LatLng(42.22560424737218, -8.73470873234326), "Synergym Torrecedeira", "<a href='https://synergym.es'>synergym.es</a>"),
            Triple(LatLng(42.231157320095875, -8.713988816439114), "Synergym Pizarro", "Description"),
            Triple(LatLng(42.23236339173375, -8.713645540064626), "A-SPORT", "Description"),
            Triple(LatLng(42.17702295077008, -8.713068309677471), "Solidd Studio", "A modern gym with state-of-the-art equipment")
        )

        gyms.forEach { (gymLocation, title, description) ->
            googleMap?.addMarker(MarkerOptions().position(gymLocation).title(title).snippet(description))
            boundsBuilder.include(gymLocation)
        }

        val bounds = boundsBuilder.build()
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

        googleMap?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View? {
                val view = LayoutInflater.from(requireContext()).inflate(R.layout.custom_info_window, null)

                val titleTextView = view.findViewById<TextView>(R.id.title)
                titleTextView.text = marker.title

                val descriptionTextView = view.findViewById<TextView>(R.id.description)

                // Verificar si el snippet es null antes de convertirlo a HTML
                val snippet = marker.snippet ?: "Sin descripción disponible"
                descriptionTextView.text = Html.fromHtml(snippet)

                return view
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
