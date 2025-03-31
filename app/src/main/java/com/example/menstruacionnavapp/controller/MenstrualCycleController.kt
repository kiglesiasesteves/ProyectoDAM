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

                    if (periodos.size > 1) {
                        val medias = calcularMedias(periodos, finPeriodos)
                        val fases = calcularFases(periodos.last().toDate(), medias.first, medias.second)
                        callback(fases)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MenstrualCycleController", "Error fetching document", e)
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

        return Pair(mediaCiclo, mediaSangrado)
    }

    private fun calcularFases(lastPeriod: Date, mediaCiclo: Int, mediaSangrado: Int): Map<String, Date> {
        val fases = mutableMapOf<String, Date>()
        val cal = Calendar.getInstance()
        cal.time = lastPeriod

        cal.add(Calendar.DAY_OF_MONTH, mediaSangrado)
        fases["Fase Folicular"] = cal.time

        cal.add(Calendar.DAY_OF_MONTH, (mediaCiclo - mediaSangrado) / 2)
        fases["Ovulación"] = cal.time

        cal.add(Calendar.DAY_OF_MONTH, 3)
        fases["Fase Lútea"] = cal.time

        cal.add(Calendar.DAY_OF_MONTH, mediaCiclo - mediaSangrado - 3)
        fases["Siguiente Menstruación"] = cal.time

        return fases
    }
}
