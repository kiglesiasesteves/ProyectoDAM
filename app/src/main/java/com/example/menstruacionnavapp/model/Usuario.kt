

import java.util.Calendar
import java.util.Date

data class Usuario(
    val promedioCiclo: Int = 0,
    val promedioSangrado: Int = 0,
    val diasParaSiguiente: Int = 0,
    val periodos: List<Date> = emptyList(),
    val finPeriodos: List<Date> = emptyList()
) {
    companion object {
        fun calcularDesde(periodos: List<Date>, finPeriodos: List<Date>): Usuario {
            if (periodos.isEmpty() || finPeriodos.isEmpty()) return Usuario()

            // Calcular promedio del ciclo
            val duraciones = periodos.zipWithNext { anterior, actual ->
                ((actual.time - anterior.time) / (1000 * 60 * 60 * 24)).toInt()
            }
            val promedioCiclo = if (duraciones.isNotEmpty()) duraciones.average().toInt() else 0

            // Calcular promedio de los días de sangrado
            val sangrados = finPeriodos.zip(periodos) { finPeriodo, inicioPeriodo ->
                ((finPeriodo.time - inicioPeriodo.time) / (1000 * 60 * 60 * 24)).toInt()
            }
            val promedioSangrado = if (sangrados.isNotEmpty()) sangrados.average().toInt() else 5

            // Calcular días para el siguiente ciclo
            val ultimoInicio = periodos.last()
            val proximoInicioEstimado = Calendar.getInstance().apply {
                time = ultimoInicio
                add(Calendar.DAY_OF_MONTH, promedioCiclo)
            }.time

            val diasParaSiguiente = ((proximoInicioEstimado.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()

            return Usuario(promedioCiclo, promedioSangrado, diasParaSiguiente, periodos, finPeriodos)
        }
    }
}
