package dev.cc231046.ccl3stepcounter.ui

import java.time.LocalDate

object DateUtils {
    fun getRelativeDayName(dayOfWeek: Int): String {
        val today = LocalDate.now()
        val todayDow = today.dayOfWeek.value

        return when (dayOfWeek) {
            todayDow -> "Today"
            getNextDay(todayDow) -> "Tomorrow"
            else -> getDayName(dayOfWeek)
        }
    }

    private fun getNextDay(current: Int): Int {
        return if (current == 7) 1 else current + 1
    }

    fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Unknown"
        }
    }

    fun getCurrentDayOfWeek(): Int {
        return LocalDate.now().dayOfWeek.value
    }

    fun getDropdownDayOptions(): List<Pair<Int, String>> {
        val today = LocalDate.now()
        val result = mutableListOf<Pair<Int, String>>()

        // Add next 7 days starting from today
        for (i in 0..6) {
            val futureDate = today.plusDays(i.toLong())
            val dayOfWeek = futureDate.dayOfWeek.value
            val label = when (i) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> getDayName(dayOfWeek)
            }
            result.add(Pair(dayOfWeek, label))
        }

        return result
    }
}