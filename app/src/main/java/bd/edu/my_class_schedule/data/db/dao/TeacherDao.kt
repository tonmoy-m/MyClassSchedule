package bd.edu.my_class_schedule.data.db.dao

import androidx.room.*
import bd.edu.my_class_schedule.data.db.entities.Teacher
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers ORDER BY name")
    fun observeAll(): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers WHERE id = :id")
    suspend fun getById(id: Long): Teacher?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(teacher: Teacher): Long

    @Delete
    suspend fun delete(teacher: Teacher)
}