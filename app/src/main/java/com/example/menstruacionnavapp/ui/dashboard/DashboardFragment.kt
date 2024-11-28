import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.menstruacionnavapp.R
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.CalendarView
import java.time.DayOfWeek
import java.time.YearMonth

class DashboardFragment : Fragment() {

    private lateinit var calendarView: CalendarView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        calendarView = view.findViewById(R.id.calendarView)

        setupCalendar()
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100) // Ajusta según sea necesario
        val endMonth = currentMonth.plusMonths(100) // Ajusta según sea necesario
        val firstDayOfWeek = firstDayOfWeekFromLocale() // Función que retorna el primer día de la semana

        // Configurar el calendario
        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        // Configurar el binder de días
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer {
                return DayViewContainer(view)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()
                // Ya no se cambia el color aquí
            }
        }
    }

    // Función para obtener el primer día de la semana según la localización
    @RequiresApi(Build.VERSION_CODES.O)
    private fun firstDayOfWeekFromLocale(): DayOfWeek {
        return DayOfWeek.MONDAY // Ajusta según tu localización
    }

}
