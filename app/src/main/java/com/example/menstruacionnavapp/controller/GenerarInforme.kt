package com.example.menstruacionnavapp.controller

import java.text.SimpleDateFormat
import java.util.*

class GenerarInforme(private val userId: String) {
    private val menstrualCycleController = MenstrualCycleController(userId)

    fun generarInforme(callback: (String) -> Unit) {
        menstrualCycleController.obtenerFases { fases ->
            if (fases != null) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val hoy = Date()
                val informe = StringBuilder()

                informe.append("Informe del Ciclo Menstrual\n")
                informe.append("Usuario ID: $userId\n")
                informe.append("Fecha de generación: ${dateFormat.format(hoy)}\n\n")

                fases.forEach { (nombreFase, fechaInicio) ->
                    val diasRestantes = ((fechaInicio.time - hoy.time) / (1000 * 60 * 60 * 24)).toInt()
                    informe.append("$nombreFase: empieza en $diasRestantes días (el ${dateFormat.format(fechaInicio)})\n")
                }

                // Añadir información sobre ejercicios
                informe.append("\nTipos de Ejercicios Recomendados:\n")
                informe.append("Menstruación: Yoga suave, caminatas\n")
                informe.append("Fase Folicular: Cardio, entrenamiento de fuerza\n")
                informe.append("Ovulación: Entrenamiento de alta intensidad\n")
                informe.append("Fase Lútea: Pilates, natación\n")

                // Añadir sección para embarazos
                val fechaOvulacion = fases["Ovulación"]
                if (fechaOvulacion != null) {
                    val diasFertilesInicio = Calendar.getInstance().apply {
                        time = fechaOvulacion
                        add(Calendar.DAY_OF_MONTH, -2)
                    }.time
                    val diasFertilesFin = Calendar.getInstance().apply {
                        time = fechaOvulacion
                        add(Calendar.DAY_OF_MONTH, 2)
                    }.time

                    informe.append("\nDías Fértiles para Embarazo:\n")
                    informe.append("Desde: ${dateFormat.format(diasFertilesInicio)} hasta: ${dateFormat.format(diasFertilesFin)}\n")
                } else {
                    informe.append("\nNo se pudo determinar los días fértiles para embarazo.\n")
                }

                callback(informe.toString())
            } else {
                callback("No se pudieron obtener las fases del ciclo menstrual.")
            }
        }
    }
}