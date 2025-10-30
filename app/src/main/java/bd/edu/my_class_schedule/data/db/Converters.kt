package bd.edu.my_class_schedule.data.db

import androidx.room.TypeConverter
import bd.edu.my_class_schedule.data.db.entities.HomeworkType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.format(dateFormatter)

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it, dateFormatter) }

    @TypeConverter
    fun localTimeToString(value: LocalTime?): String? = value?.format(timeFormatter)

    @TypeConverter
    fun stringToLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it, timeFormatter) }

    @TypeConverter
    fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun longToInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    // NEW: HomeworkType enum storage as String
    @TypeConverter
    fun homeworkTypeToString(value: HomeworkType?): String? = value?.name

    @TypeConverter
    fun stringToHomeworkType(value: String?): HomeworkType? =
        value?.let { runCatching { HomeworkType.valueOf(it) }.getOrNull() }
}