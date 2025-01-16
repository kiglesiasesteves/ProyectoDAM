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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker

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

            // Create a LatLngBounds builder
            val boundsBuilder = LatLngBounds.builder()

            // Add markers for the gyms (replace with actual gym locations)
            val gyms = listOf(
                Triple(LatLng(42.22560424737218, -8.73470873234326), "Synergym Torrecedeira", "<a href='https://synergym.es'>synergym.es</a>"),
                Triple(LatLng(42.22640424737218, -8.73570873234326), "Gimnasio del Pueblo", "Description"),
                Triple(LatLng(42.22760424737218, -8.73670873234326), "Con mucha fuerza", "Description"),
                Triple(LatLng(42.22860424737218, -8.73770873234326), "Gym Alpha", "A modern gym with state-of-the-art equipment"),
                Triple(LatLng(42.22960424737218, -8.73870873234326), "FitNation", "Your go-to place for fitness goals"),
                Triple(LatLng(42.23060424737218, -8.73970873234326), "PowerHouse Gym", "Strength training, cardio, and more"),
                Triple(LatLng(42.23160424737218, -8.74070873234326), "BodyFlex", "Yoga, pilates, and fitness classes"),
                Triple(LatLng(42.23260424737218, -8.74170873234326), "Gym Force", "CrossFit and intensive workouts"),
                Triple(LatLng(42.23360424737218, -8.74270873234326), "Elite Fitness Center", "Exclusive gym with VIP services"),
                Triple(LatLng(42.23460424737218, -8.74370873234326), "ActiveZone", "A fun and energetic fitness environment"),
                Triple(LatLng(42.23560424737218, -8.74470873234326), "The Fitness Loft", "Comprehensive gym with personal training"),
                Triple(LatLng(42.23660424737218, -8.74570873234326), "Ultimate Fit", "Where fitness meets performance"),
                Triple(LatLng(42.23760424737218, -8.74670873234326), "Xtreme Fitness", "Extreme workouts for extreme results"),
                Triple(LatLng(42.23860424737218, -8.74770873234326), "Flex Gym", "Flexibility and strength training combined"),
                Triple(LatLng(42.23960424737218, -8.74870873234326), "Peak Fitness", "Achieve your peak performance")
            )

            gyms.forEach { (gymLocation, title, description) ->
                googleMap.addMarker(MarkerOptions().position(gymLocation).title(title).snippet(description))
                boundsBuilder.include(gymLocation) // Add each gym location to the bounds
            }

            // Move camera to show all gyms with a padding of 100 pixels
            val bounds = boundsBuilder.build()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

            // Set a custom InfoWindowAdapter to render HTML and clickable links
            googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                override fun getInfoWindow(marker: Marker): View? {
                    return null // We don't need a custom window, just the default
                }

                override fun getInfoContents(marker: Marker): View? {
                    // Custom view for info contents
                    val view = LayoutInflater.from(requireContext()).inflate(R.layout.custom_info_window, null)

                    // Set the title (marker title)
                    val titleTextView = view.findViewById<TextView>(R.id.title)
                    titleTextView.text = marker.title

                    // Set the description (marker snippet) - This will be the HTML link
                    val descriptionTextView = view.findViewById<TextView>(R.id.description)
                    descriptionTextView.text = Html.fromHtml(marker.snippet) // Use Html.fromHtml to render HTML

                    return view
                }
            })

            // Get the current location of the device
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
