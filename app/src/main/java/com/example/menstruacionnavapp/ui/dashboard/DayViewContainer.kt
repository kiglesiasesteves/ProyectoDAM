import android.view.View
import android.widget.TextView
import com.example.menstruacionnavapp.R
import com.kizitonwose.calendar.view.ViewContainer


class DayViewContainer(view: View) : ViewContainer(view) {
    val textView: TextView = view.findViewById(R.id.calendarDayText)

    // Puedes usar ViewBinding si lo prefieres.
    // val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
}
