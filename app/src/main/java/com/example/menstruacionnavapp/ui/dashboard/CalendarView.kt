package com.example.menstruacionnavapp.ui.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.YearMonth

class CalendarView {
    @RequiresApi(Build.VERSION_CODES.O)
    val currentMonth = YearMonth.now()
    @RequiresApi(Build.VERSION_CODES.O)
    val startMonth = currentMonth.minusMonths(100) // Adjust as needed
    @RequiresApi(Build.VERSION_CODES.O)
    val endMonth = currentMonth.plusMonths(100) // Adjust as needed
    val firstDayOfWeek = firstDayOfWeekFromLocale() // Available from the library

}