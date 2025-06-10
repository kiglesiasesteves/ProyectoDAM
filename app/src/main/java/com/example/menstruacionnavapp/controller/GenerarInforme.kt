package com.example.menstruacionnavapp.controller

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class GenerarInforme(private val userId: String, private val context: Context) {
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

                // Generar PDF
                generarPDF(informe.toString(), callback)
            } else {
                callback("No se pudieron obtener las fases del ciclo menstrual.")
            }
        }
    }

    private fun generarPDF(contenido: String, callback: (String) -> Unit) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        val paint = android.graphics.Paint()
        val x = 10
        var y = 25

        contenido.split("\n").forEach { line ->
            canvas.drawText(line, x.toFloat(), y.toFloat(), paint)
            y += (paint.descent() - paint.ascent()).toInt()
        }

        pdfDocument.finishPage(page)

        val directoryPath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath
        val file = File(directoryPath, "InformeCicloMenstrual.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            callback("Informe generado y guardado en: ${file.absolutePath}")
        } catch (e: Exception) {
            callback("Error al generar el PDF: ${e.message}")
        } finally {
            pdfDocument.close()
        }
    }

    // Nuevo método
    fun guardarInformeEn(uri: android.net.Uri, callback: (String) -> Unit) {
        generarContenidoInforme { contenido ->
            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = pdfDocument.startPage(pageInfo)

                val canvas = page.canvas
                val paint = android.graphics.Paint()
                val x = 10
                var y = 25

                contenido.split("\n").forEach { line ->
                    canvas.drawText(line, x.toFloat(), y.toFloat(), paint)
                    y += (paint.descent() - paint.ascent()).toInt()
                }

                pdfDocument.finishPage(page)

                pdfDocument.writeTo(outputStream!!)
                pdfDocument.close()
                outputStream.close()

                callback("Informe guardado correctamente en el archivo seleccionado.")
            } catch (e: Exception) {
                callback("Error al guardar el PDF: ${e.message}")
            }
        }
    }

    // Separamos la generación del contenido del PDF para reutilizarlo
    private fun generarContenidoInforme(callback: (String) -> Unit) {
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

                informe.append("\nTipos de Ejercicios Recomendados:\n")
                informe.append("Menstruación: Yoga suave, caminatas\n")
                informe.append("Fase Folicular: Cardio, entrenamiento de fuerza\n")
                informe.append("Ovulación: Entrenamiento de alta intensidad\n")
                informe.append("Fase Lútea: Pilates, natación\n")

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
                callback("No se pudieron obtener las fases del ciclo.")
            }
        }
    }

}