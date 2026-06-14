package edu.nd.pmcburne.hwapp.one.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatGameDateTime(timestamp: Timestamp): String {
    val gameDate = timestamp.toDate()
    val calendar = Calendar.getInstance().apply { time = gameDate }
    val today = Calendar.getInstance()
    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(gameDate)

    val sameYear = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
    val dayDiff = calendar.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR)
    return when {
        sameYear && dayDiff == 0 -> "Today \u00B7 $timeStr"
        sameYear && dayDiff == 1 -> "Tomorrow \u00B7 $timeStr"
        else -> {
            val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            "${dateFormat.format(gameDate)} \u00B7 $timeStr"
        }
    }
}

fun formatMonthYear(timestamp: Timestamp): String =
    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(timestamp.toDate())

fun formatChatTime(timestamp: Timestamp): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(timestamp.toDate())

fun monthYearOf(date: Date): String =
    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)

fun distanceMilesString(distanceMeters: Float): String {
    val miles = distanceMeters * 0.000621371f
    return when {
        miles < 0.1f -> "<0.1 mi"
        else -> String.format(Locale.getDefault(), "%.1f mi", miles)
    }
}
