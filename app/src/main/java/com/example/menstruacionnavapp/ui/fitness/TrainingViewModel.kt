package com.example.menstruacionnavapp.ui.register.com.example.menstruacionnavapp.ui.GenerarEntrenamientos


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.menstruacionnavapp.ui.register.com.example.menstruacionnavapp.model.EntornoEntrenamiento
import com.example.menstruacionnavapp.ui.register.com.example.menstruacionnavapp.model.FaseCiclo


class TrainingViewModel : ViewModel() {
    private val _entrenamiento = MutableLiveData<EntornoEntrenamiento>()
    val entrenamiento: LiveData<EntornoEntrenamiento> get() = _entrenamiento

    // Función que genera el entrenamiento según la fase en la que se encuentra la persona.
    fun generarEntrenamiento(faseCiclo: FaseCiclo) {
        // Aquí se define qué entrenamiento corresponde a cada fase del ciclo.
        val entrenamiento = when(faseCiclo) {
            FaseCiclo.MENSTRUAL -> EntornoEntrenamiento(
                ejercicio = "Pilates o Yoga",
                descripcion = "Durante la fase menstrual, se recomienda realizar ejercicios suaves para no sobrecargar el cuerpo.",
                videoUrl = "https://www.youtube.com/watch?v=e4IF5uaGMyA"
            )
            FaseCiclo.OVULATORIA -> EntornoEntrenamiento(
                ejercicio = "Entrenamiento de alta intensidad",
                descripcion = "En la fase ovulatoria, el cuerpo tiene más energía, por lo que puedes realizar entrenamientos de alta intensidad.",
                videoUrl = "https://www.youtube.com/watch?v=_AhC7mm-Prk"
            )
            FaseCiclo.FOLICULAR -> EntornoEntrenamiento(
                ejercicio = "Entrenamiento de fuerza",
                descripcion = "Durante la fase folicular, es adecuado realizar ejercicios de fuerza moderada a alta.",
                videoUrl = "https://www.youtube.com/watch?v=jYwHKLo75kc"
            )
            FaseCiclo.LUTEA -> EntornoEntrenamiento(
                ejercicio = "Cardio ligero o caminatas",
                descripcion = "Durante la fase lutea, el cuerpo se beneficia de un ejercicio moderado para evitar sobrecargas.",
                videoUrl = "https://www.youtube.com/watch?v=JgtR-Xoh9d0"
            )
        }

        _entrenamiento.value = entrenamiento
    }
}
