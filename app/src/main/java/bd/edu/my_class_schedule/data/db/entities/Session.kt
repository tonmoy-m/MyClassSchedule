package bd.edu.my_class_schedule.data.db.entities

import androidx.room.*
import java.time.LocalTime

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Teacher::class,
            parentColumns = ["id"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["day_of_week", "start_time"]),
        Index(value = ["subject_id"]),
        Index(value = ["teacher_id"])
    ]
)
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "subject_id") val subjectId: Long,
    @ColumnInfo(name = "teacher_id") val teacherId: Long? = null,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: Int, // 1=Mon ... 7=Sun (ISO)
    @ColumnInfo(name = "start_time") val startTime: LocalTime,
    @ColumnInfo(name = "end_time") val endTime: LocalTime,
    @ColumnInfo(name = "week_type", defaultValue = "0") val weekType: Int = 0, // 0=both,1=odd,2=even
    @ColumnInfo(name = "room") val room: String? = null,
    @ColumnInfo(name = "reminder_minutes_before") val reminderMinutesBefore: Int? = null,
    @ColumnInfo(name = "silence_dnd", defaultValue = "0") val silenceDnd: Boolean = false
) {
    companion object {
        const val BOTH = 0
        const val ODD = 1
        const val EVEN = 2
    }
}