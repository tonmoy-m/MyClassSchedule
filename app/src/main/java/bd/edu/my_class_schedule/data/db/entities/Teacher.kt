package bd.edu.my_class_schedule.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "teachers",
    indices = [
        Index(value = ["name"]),
        Index(value = ["dept"])
    ]
)
data class Teacher(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "designation") val designation: String? = null,
    @ColumnInfo(name = "dept") val dept: String? = null,
    @ColumnInfo(name = "personal_email") val personalEmail: String? = null,
    @ColumnInfo(name = "official_email") val officialEmail: String? = null,
    @ColumnInfo(name = "personal_phone") val personalPhone: String? = null,
    @ColumnInfo(name = "official_phone") val officialPhone: String? = null,
    @ColumnInfo(name = "notes") val notes: String? = null
)