package com.example.menstruacionnavapp

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.menstruacionnavapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // View binding para la actividad principal
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el layout usando view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener la vista de navegación inferior
        val navView: BottomNavigationView = binding.navView

        // Encontrar el NavController asociado con el NavHostFragment
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Pasar cada ID de menú como un conjunto de IDs porque cada
        // menú debe ser considerado como un destino de nivel superior.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        // Configurar la barra de acción con el NavController y la configuración de la barra de aplicación
        setupActionBarWithNavController(navController, appBarConfiguration)
        // Configurar la vista de navegación inferior con el NavController
        navView.setupWithNavController(navController)
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}