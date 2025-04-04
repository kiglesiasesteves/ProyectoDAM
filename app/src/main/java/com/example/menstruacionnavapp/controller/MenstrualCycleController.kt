package com.example.menstruacionnavapp.controller

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.math.roundToInt

class MenstrualCycleController(private val userId: String) {
    private val db = FirebaseFirestore.getInstance()

    fun obtenerFases(callback: (Map<String, Date>?) -> Unit) {
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val periodos = document.get("periodos") as? List<Timestamp> ?: emptyList()
                    val finPeriodos = document.get("finPeriodos") as? List<Timestamp> ?: emptyList()

                    Log.d("MenstrualCycleController", "Document exists. Periodos: $periodos, FinPeriodos: $finPeriodos")

                    if (periodos.size > 1) {
                        val medias = calcularMedias(periodos, finPeriodos)
                        val fases = calcularFases(periodos.last().toDate(), medias.first, medias.second)
                        Log.d("MenstrualCycleController", "Fases calculadas: $fases")
                        callback(fases)
                    } else {
                        Log.d("MenstrualCycleController", "No hay suficientes periodos para calcular fases.")
                        callback(null)
                    }
                } else {
                    Log.d("MenstrualCycleController", "Document does not exist.")
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MenstrualCycleController", "Error fetching document", e)
                callback(null)
            }
    }

    fun obtenerFaseActual(callback: (String?) -> Unit) {
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val periodos = document.get("periodos") as? List<Timestamp> ?: emptyList()
                    val finPeriodos = document.get("finPeriodos") as? List<Timestamp> ?: emptyList()

                    Log.d("MenstrualCycleController", "Periodos: $periodos")
                    Log.d("MenstrualCycleController", "FinPeriodos: $finPeriodos")

                    if (periodos.isNotEmpty() && finPeriodos.isNotEmpty()) {
                        val hoy = Date()

                        val periodosArray = periodos.map { it.toDate() }.toTypedArray()
                        val finPeriodosArray = finPeriodos.map { it.toDate() }.toTypedArray()

                        if (periodosArray.isNotEmpty() && finPeriodosArray.isNotEmpty()) {
                            val ultimoInicio = periodosArray.last()
                            val ultimoFin = finPeriodosArray.lastOrNull() ?: return@addOnSuccessListener

                            if (ultimoFin.before(ultimoInicio)) {
                                Log.d("MenstrualCycleController", "Verificando período menstrual: inicio = $ultimoInicio, fin = $ultimoFin, hoy = $hoy")
                                    Log.d("MenstrualCycleController", "Fase actual: Menstruación")
                                    callback("Menstruación")
                                    return@addOnSuccessListener

                            } else {
                                Log.d("MenstrualCycleController", "El último fin es antes del último inicio, no es un período válido.")
                            }
                        }

                        if (periodos.size > 1) {
                            val medias = calcularMedias(periodos, finPeriodos)
                            val fases = calcularFases(periodos.last().toDate(), medias.first, medias.second)

                            // Verificar en qué fase estamos
                            val fechas = listOf(
                                "Fase Folicular" to fases["Fase Folicular"],
                                "Ovulación" to fases["Ovulación"],
                                "Fase Lútea" to fases["Fase Lútea"],
                                "Siguiente Menstruación" to fases["Siguiente Menstruación"]
                            )

                            for (i in fechas.indices) {
                                val faseActual = fechas[i].second ?: continue
                                val faseSiguiente = fechas.getOrNull(i + 1)?.second

                                if (faseSiguiente != null && hoy >= faseActual && hoy < faseSiguiente) {
                                    Log.d("MenstrualCycleController", "Fase actual: ${fechas[i].first}")
                                    callback(fechas[i].first)
                                    return@addOnSuccessListener
                                }
                            }

                            // Si no cae en ninguna, puede que esté justo el día antes del siguiente ciclo
                            Log.d("MenstrualCycleController", "Fase actual: Fase Lútea")
                            callback("Fase Lútea")
                        } else {
                            callback(null)
                        }
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    private fun calcularMedias(periodos: List<Timestamp>, finPeriodos: List<Timestamp>): Pair<Int, Int> {
        val ciclos = mutableListOf<Int>()
        val sangrados = mutableListOf<Int>()

        for (i in 1 until periodos.size) {
            val diasCiclo = ((periodos[i].toDate().time - periodos[i - 1].toDate().time) / (1000 * 60 * 60 * 24)).toInt()
            ciclos.add(diasCiclo)
        }

        for (i in finPeriodos.indices) {
            val diasSangrado = ((finPeriodos[i].toDate().time - periodos[i].toDate().time) / (1000 * 60 * 60 * 24)).toInt()
            sangrados.add(diasSangrado)
        }

        val mediaCiclo = if (ciclos.isNotEmpty()) (ciclos.average().roundToInt()) else 28
        val mediaSangrado = if (sangrados.isNotEmpty()) (sangrados.average().roundToInt()) else 5

        Log.d("MenstrualCycleController", "Medias calculadas: mediaCiclo = $mediaCiclo, mediaSangrado = $mediaSangrado")

        return Pair(mediaCiclo, mediaSangrado)
    }

    private fun calcularFases(lastPeriod: Date, mediaCiclo: Int, mediaSangrado: Int): Map<String, Date> {
        val fases = mutableMapOf<String, Date>()
        val cal = Calendar.getInstance()
        cal.time = lastPeriod

        // Fase Menstrual
        fases["Menstruación"] = cal.time
        Log.d("MenstrualCycleController", "Fase Menstruación: ${cal.time}")

        // Fase Folicular
        cal.add(Calendar.DAY_OF_MONTH, mediaSangrado)
        fases["Fase Folicular"] = cal.time
        Log.d("MenstrualCycleController", "Fase Folicular: ${cal.time}")

        // Ovulación
        cal.add(Calendar.DAY_OF_MONTH, (mediaCiclo - mediaSangrado) / 2)
        fases["Ovulación"] = cal.time
        Log.d("MenstrualCycleController", "Ovulación: ${cal.time}")

        // Fase Lútea
        cal.add(Calendar.DAY_OF_MONTH, 3)
        fases["Fase Lútea"] = cal.time
        Log.d("MenstrualCycleController", "Fase Lútea: ${cal.time}")

        // Siguiente Menstruación
        cal.add(Calendar.DAY_OF_MONTH, mediaCiclo - mediaSangrado - 3)
        fases["Siguiente Menstruación"] = cal.time
        Log.d("MenstrualCycleController", "Siguiente Menstruación: ${cal.time}")

        Log.d("MenstrualCycleController", "Fases calculadas: $fases")

        return fases
    }
}