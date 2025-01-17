package com.example.menstruacionnavapp

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.menstruacionnavapp.databinding.ActivityMainBinding
import com.example.menstruacionnavapp.ui.register.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    // View binding para la actividad principal
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // Inflar el layout usando view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar si el usuario ya está autenticado
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // Si el usuario no está autenticado, redirigir a la actividad de registro
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish() // Para asegurarte de que la MainActivity no se mantenga en la pila
            return
        }

        // Obtener la vista de navegación inferior
        val navView: BottomNavigationView = binding.navView

        // Encontrar el NavController asociado con el NavHostFragment
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Pasar cada ID de menú como un conjunto de IDs porque cada menú debe ser considerado como un destino de nivel superior.
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
}
