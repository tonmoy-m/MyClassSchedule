package bd.edu.my_class_schedule.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    // Keep subject as display name; chosen from Subjects dropdown
    val subject: String? = null,
    val location: String? = null,
    val notes: String? = null,

    // NEW: date + start/end time (nullable to allow drafts)
    @ColumnInfo(name = "exam_date") val date: LocalDate? = null,
    @ColumnInfo(name = "start_time") val startTime: LocalTime? = null,
    @ColumnInfo(name = "end_time") val endTime: LocalTime? = null,

    val createdAt: Long,
    val updatedAt: Long
)