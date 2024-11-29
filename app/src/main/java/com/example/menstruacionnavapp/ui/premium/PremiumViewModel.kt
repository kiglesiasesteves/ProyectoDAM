package com.example.menstruacionnavapp.ui.premium

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel


class PremiumViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = """
            🌟 ¡Bienvenido a **CicloFit Premium**! 🌟
            
            🚴‍♂️ **Descubre nuevas funcionalidades exclusivas:**
            - 🏋️ Planes de entrenamiento personalizados.
            - 📈 Estadísticas avanzadas de tu progreso.
            - 🎵 Integración con tus playlists favoritas.
            
            💳 **Activa tu cuenta Premium hoy mismo:**
            Realiza tu pago de suscripción de forma fácil y segura a través de **PayPal**.  
            
            🔓 ¡Desbloquea todo tu potencial con CicloFit Premium! 💪
        """.trimIndent()
    }
    val text: LiveData<String> = _text

}
