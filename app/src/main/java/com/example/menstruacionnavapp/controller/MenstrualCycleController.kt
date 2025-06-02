package com.example.menstruacionnavapp.controller

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.math.abs
import kotlin.math.max
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

                    if (periodos.isNotEmpty()) {
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

                    if (periodos.isEmpty()) {
                        callback(null)
                        return@addOnSuccessListener
                    }

                    val hoy = Date()

                    // Ordenar periodos y finPeriodos cronológicamente
                    val periodosOrdenados = periodos.map { it.toDate() }.sortedBy { it.time }
                    val finPeriodosOrdenados = finPeriodos.map { it.toDate() }.sortedBy { it.time }

                    // Verificar si estamos en un periodo activo (inicio registrado sin fin)
                    if (periodosOrdenados.size > finPeriodosOrdenados.size) {
                        val ultimoInicio = periodosOrdenados.last()
                        if (hoy.time >= ultimoInicio.time) {
                            Log.d("MenstrualCycleController", "Fase actual: Menstruación (periodo activo)")
                            callback("Menstruación")
                            return@addOnSuccessListener
                        }
                    }

                    // Verificar si estamos entre un inicio y fin de periodo
                    for (i in finPeriodosOrdenados.indices) {
                        val inicio = periodosOrdenados.getOrNull(i) ?: continue
                        val fin = finPeriodosOrdenados[i]

                        if (hoy.time in inicio.time..fin.time) {
                            Log.d("MenstrualCycleController", "Fase actual: Menstruación (dentro de un periodo)")
                            callback("Menstruación")
                            return@addOnSuccessListener
                        }
                    }

                    // Si no estamos en periodo menstrual, calculamos en qué fase estamos
                    val ultimoPeriodo = if (finPeriodosOrdenados.isNotEmpty()) {
                        // Usar el último periodo completo como referencia
                        val ultimoIndiceCompleto = minOf(periodosOrdenados.size, finPeriodosOrdenados.size) - 1
                        if (ultimoIndiceCompleto >= 0) {
                            finPeriodosOrdenados[ultimoIndiceCompleto]
                        } else {
                            periodosOrdenados.last()
                        }
                    } else {
                        periodosOrdenados.last()
                    }

                    val medias = calcularMedias(periodos, finPeriodos)
                    val fases = calcularFases(ultimoPeriodo, medias.first, medias.second)

                    // Determinar en qué fase estamos ahora
                    val fasesOrdenadas = listOf(
                        "Menstruación" to fases["Menstruación"],
                        "Fase Folicular" to fases["Fase Folicular"],
                        "Ovulación" to fases["Ovulación"],
                        "Fase Lútea" to fases["Fase Lútea"],
                        "Siguiente Menstruación" to fases["Siguiente Menstruación"]
                    ).sortedBy { it.second?.time ?: 0 }

                    for (i in 0 until fasesOrdenadas.size - 1) {
                        val faseActual = fasesOrdenadas[i]
                        val faseSiguiente = fasesOrdenadas[i + 1]

                        val inicioFase = faseActual.second ?: continue
                        val finFase = faseSiguiente.second ?: continue

                        if (hoy.time in inicioFase.time until finFase.time) {
                            Log.d("MenstrualCycleController", "Fase actual: ${faseActual.first}")
                            callback(faseActual.first)
                            return@addOnSuccessListener
                        }
                    }

                    // Si no caemos en ninguna fase, probablemente estamos en la última
                    val ultimaFase = fasesOrdenadas.last()
                    Log.d("MenstrualCycleController", "Fase actual (última): ${ultimaFase.first}")
                    callback(ultimaFase.first)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MenstrualCycleController", "Error obteniendo fase actual", e)
                callback(null)
            }
    }

    private fun calcularMedias(periodos: List<Timestamp>, finPeriodos: List<Timestamp>): Pair<Int, Int> {
        val ciclos = mutableListOf<Int>()
        val sangrados = mutableListOf<Int>()

        // Calcular duración de ciclos (de inicio a inicio)
        for (i in 1 until periodos.size) {
            val diasCiclo = ((periodos[i].toDate().time - periodos[i - 1].toDate().time) / (1000 * 60 * 60 * 24)).toInt()
            if (diasCiclo > 0 && diasCiclo <= 45) { // Filtrar ciclos muy cortos o muy largos
                ciclos.add(diasCiclo)
            }
        }

        // Calcular duración de sangrados (de inicio a fin)
        val minSize = minOf(periodos.size, finPeriodos.size)
        for (i in 0 until minSize) {
            val diasSangrado = ((finPeriodos[i].toDate().time - periodos[i].toDate().time) / (1000 * 60 * 60 * 24)).toInt()
            if (diasSangrado > 0 && diasSangrado <= 10) { // Filtrar duraciones anómalas
                sangrados.add(diasSangrado)
            }
        }

        val mediaCiclo = if (ciclos.isNotEmpty()) (ciclos.average().roundToInt()) else 28
        val mediaSangrado = if (sangrados.isNotEmpty()) (sangrados.average().roundToInt()) else 5

        Log.d("MenstrualCycleController", "Medias calculadas: mediaCiclo = $mediaCiclo, mediaSangrado = $mediaSangrado")

        return Pair(mediaCiclo, mediaSangrado)
    }

    private fun calcularFases(ultimoPeriodo: Date, mediaCiclo: Int, mediaSangrado: Int): Map<String, Date> {
        val fases = mutableMapOf<String, Date>()
        val cal = Calendar.getInstance()

        // Asegurarnos de que el ciclo tenga una duración mínima razonable
        val duracionCiclo = max(21, mediaCiclo)
        val duracionSangrado = max(1, mediaSangrado)

        // Calcular la fecha de la última menstruación
        cal.time = ultimoPeriodo
        fases["Menstruación"] = cal.time
        Log.d("MenstrualCycleController", "Fecha Menstruación: ${cal.time}")

        // Fase Folicular (después de la menstruación)
        cal.add(Calendar.DAY_OF_MONTH, duracionSangrado)
        fases["Fase Folicular"] = cal.time
        Log.d("MenstrualCycleController", "Fecha Fase Folicular: ${cal.time}")

        // Ovulación (a mitad del ciclo, descontando los días de sangrado)
        val diasHastaOvulacion = (duracionCiclo - duracionSangrado) / 2
        cal.add(Calendar.DAY_OF_MONTH, max(1, diasHastaOvulacion))
        fases["Ovulación"] = cal.time
        Log.d("MenstrualCycleController", "Fecha Ovulación: ${cal.time}")

        // Fase Lútea (después de la ovulación)
        cal.add(Calendar.DAY_OF_MONTH, 3)
        fases["Fase Lútea"] = cal.time
        Log.d("MenstrualCycleController", "Fecha Fase Lútea: ${cal.time}")

        // Siguiente Menstruación
        val diasRestantes = duracionCiclo - duracionSangrado - diasHastaOvulacion - 3
        cal.add(Calendar.DAY_OF_MONTH, max(1, diasRestantes))
        fases["Siguiente Menstruación"] = cal.time
        Log.d("MenstrualCycleController", "Fecha Siguiente Menstruación: ${cal.time}")

        return fases
    }
}
