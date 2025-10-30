package bd.edu.my_class_schedule.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "homework")
data class Homework(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val subject: String? = null,          // Stores the subject name selected from Subjects
    val description: String? = null,
    val dueDateMillis: Long? = null,      // Date-only millis as you used before
    val isCompleted: Boolean = false,
    val type: HomeworkType = HomeworkType.HOMEWORK, // NEW: categorize
    val createdAt: Long,
    val updatedAt: Long
)