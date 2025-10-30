package bd.edu.my_class_schedule.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subjects",
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["title"])
    ]
)
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val code: String,              // course code (required, unique)
    val title: String,             // course title (required)
    val credit: Double,            // credit (required, positive)
    val shortName: String? = null  // optional short name
)