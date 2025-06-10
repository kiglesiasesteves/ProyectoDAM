package com.example.menstruacionnavapp.ui.buscarGimnasios

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Html
import android.text.util.Linkify
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

// Fragmento para mostrar el mapa de gimnasios y la ubicación del usuario.
class GymFragment : Fragment() {
    private var _binding: FragmentGymsBinding? = null // Referencia al binding para acceder a las vistas.
    private val binding get() = _binding!! // Acceso seguro al binding.
    private lateinit var fusedLocationClient: FusedLocationProviderClient // Cliente para obtener la ubicación.
    private var googleMap: GoogleMap? = null // Variable para almacenar el objeto de GoogleMap.
    private val LOCATION_PERMISSION_REQUEST_CODE = 1 // Código de solicitud de permisos para la ubicación.

    // Método que se llama cuando se crea la vista del fragmento.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Se obtiene el ViewModel de este fragmento.
        val gymViewModel = ViewModelProvider(this).get(GymViewModel::class.java)

        // Se infla el layout del fragmento utilizando el binding.
        _binding = FragmentGymsBinding.inflate(inflater, container, false)
        val root: View = binding.root // Se obtiene la vista raíz del binding.

        // Inicializa el cliente de ubicación.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Se obtiene la referencia al fragmento de mapa y se configura el mapa.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback { googleMap ->
            this.googleMap = googleMap // Asignación del objeto GoogleMap.
            checkLocationPermission() // Verifica si tiene permisos para acceder a la ubicación.
            setupMapMarkers() // Configura los marcadores de gimnasios en el mapa.
        })

        return root // Retorna la vista del fragmento.
    }

    // Método que verifica si se tiene el permiso para acceder a la ubicación del dispositivo.
    private fun checkLocationPermission() {
        // Si no tiene el permiso para acceder a la ubicación, solicita el permiso.
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
            // Si ya tiene el permiso, habilita la ubicación del usuario en el mapa.
            enableUserLocation()
        }
    }

    // Método que habilita la ubicación del usuario en el mapa.
    private fun enableUserLocation() {
        // Verifica si el permiso está concedido.
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Habilita la capa de la ubicación del usuario en el mapa.
            googleMap?.isMyLocationEnabled = true

            // Obtiene la ubicación actual del usuario utilizando la API de ubicación de Google.
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, null
            ).addOnSuccessListener { location ->
                if (location != null) {
                    // Obtiene las coordenadas de la ubicación y mueve la cámara para centrarla en el usuario.
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                }
            }

        }
    }

    // Método que maneja el resultado de la solicitud de permisos.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Si el código de solicitud es el correcto y se concede el permiso, habilita la ubicación del usuario.
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation()
            }
        }
    }

    private fun setupMapMarkers() {
        // Crea un LatLngBounds.Builder para ajustar la cámara del mapa para que incluya todos los gimnasios.
        val boundsBuilder = LatLngBounds.builder()

        // Lista de gimnasios con su ubicación, nombre y descripción.
        val gyms = listOf(
            Triple(LatLng(42.22560424737218, -8.73470873234326), "Synergym Torrecedeira", "<a href='https://synergym.es'>synergym</a>"),
            Triple(LatLng(42.231157320095875, -8.713988816439114), "Synergym Pizarro", "Description"),
            Triple(LatLng(42.23236339173375, -8.713645540064626), "A-SPORT", "Description"),
            Triple(LatLng(42.17702295077008, -8.713068309677471), "Solidd Studio", "A modern gym with state-of-the-art equipment")
        )

        // Itera sobre la lista de gimnasios y agrega un marcador por cada gimnasio en el mapa.
        gyms.forEach { (gymLocation, title, description) ->
            googleMap?.addMarker(MarkerOptions().position(gymLocation).title(title).snippet(description))
            boundsBuilder.include(gymLocation) // Añade la ubicación del gimnasio al LatLngBounds.
        }

        // Obtiene los límites de la cámara que incluirán todos los gimnasios.
        val bounds = boundsBuilder.build()
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)) // Ajusta la cámara para incluir todos los gimnasios.

        // Configura el adaptador de la ventana de información de los marcadores.
        googleMap?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            // Este método es para la ventana de información personalizada del marcador.
            override fun getInfoWindow(marker: Marker): View? {
                return null // Se maneja el contenido en otro método, no es necesario modificar la ventana de información por defecto.
            }

            // Este método personaliza el contenido de la ventana de información del marcador.
            override fun getInfoContents(marker: Marker): View? {
                // Infla la vista personalizada de la ventana de información.
                val view = LayoutInflater.from(requireContext()).inflate(R.layout.custom_info_window, null)

                // Obtiene la vista del título y asigna el título del marcador.
                val titleTextView = view.findViewById<TextView>(R.id.title)
                titleTextView.text = marker.title

                // Obtiene la vista de la descripción y asigna la descripción del marcador.
                val descriptionTextView = view.findViewById<TextView>(R.id.description)

                // Verifica si el snippet (descripción) es null y lo convierte a HTML.
                val snippet = marker.snippet ?: "Sin descripción disponible"
                descriptionTextView.text = Html.fromHtml(snippet, Html.FROM_HTML_MODE_LEGACY) // Convierte el texto en HTML y lo asigna a la vista de descripción.

                // Activar los enlaces dentro de la descripción (HTML)
                Linkify.addLinks(descriptionTextView, Linkify.WEB_URLS)

                return view // Retorna la vista personalizada.
            }
        })
    }

    // Método que se llama cuando la vista del fragmento se destruye, para liberar recursos.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpia el binding para evitar fugas de memoria.
    }
}
