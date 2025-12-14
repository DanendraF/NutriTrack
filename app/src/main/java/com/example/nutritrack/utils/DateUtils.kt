package com.example.nutritrack.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val DISPLAY_DATE_FORMAT = "dd MMM yyyy"
    private const val TIME_FORMAT = "HH:mm"
    private const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

    /**
     * Get current date in yyyy-MM-dd format
     */
    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Get current timestamp
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Format date for display (e.g., "14 Dec 2025")
     */
    fun formatDateForDisplay(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val outputFormat = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Format timestamp to time string (e.g., "14:30")
     */
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Format timestamp to date string
     */
    fun formatTimestampToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Get date X days ago
     */
    fun getDateDaysAgo(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return sdf.format(calendar.time)
    }

    /**
     * Check if date is today
     */
    fun isToday(dateString: String): Boolean {
        return dateString == getCurrentDate()
    }

    /**
     * Get relative date string (Today, Yesterday, or date)
     */
    fun getRelativeDateString(dateString: String): String {
        val today = getCurrentDate()
        val yesterday = getDateDaysAgo(1)

        return when (dateString) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> formatDateForDisplay(dateString)
        }
    }

    /**
     * Get day of week (e.g., "Monday")
     */
    fun getDayOfWeek(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Generate unique ID with timestamp
     */
    fun generateId(): String {
        return UUID.randomUUID().toString()
    }
}
