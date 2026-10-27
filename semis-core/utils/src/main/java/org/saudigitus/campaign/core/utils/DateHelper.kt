package org.saudigitus.campaign.core.utils

import org.dhis2.commons.date.DateUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateHelper {
    fun formatDate(date: Long): String? {
        return try {
            val formatter = SimpleDateFormat(DateUtils.DATE_FORMAT_EXPRESSION, Locale.US)
            formatter.format(Date(date))
        } catch (e: Exception) {
            Timber.tag("DATE_FORMAT").e(e)
            null
        }
    }

    fun formatDateWithWeekDay(date: Long): String? {
        return try {
            val strDate = formatDate(date).orEmpty()

            val inputFormat = SimpleDateFormat(DateUtils.DATE_FORMAT_EXPRESSION, Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }

            val inputDate: Date = inputFormat.parse(strDate)!!
            outputFormat.format(inputDate)
        } catch (e: Exception) {
            Timber.tag("DATE_FORMAT").e(e)
            null
        }
    }

    fun formatDateWithWeekDay(date: String): String? {
        return try {
            val inputFormat = SimpleDateFormat(DateUtils.DATE_FORMAT_EXPRESSION, Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }

            val inputDate: Date = inputFormat.parse(date)!!
            outputFormat.format(inputDate)
                .replace(".", "")
                .replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
        } catch (e: Exception) {
            Timber.tag("DATE_FORMAT").e(e)
            null
        }
    }

    fun isWeekend(date: LocalDate): Boolean {
        val dayOfWeek = date.dayOfWeek
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
    }

    fun stringToLocalDate(date: String): LocalDate {
        return LocalDate.parse(date)
    }

    fun convertDateToMilliseconds(dateString: String): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val localDate = LocalDate.parse(dateString, formatter)

        val zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault())
        return zonedDateTime.toInstant().toEpochMilli()
    }


}