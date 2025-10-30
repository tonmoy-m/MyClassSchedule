package bd.edu.my_class_schedule.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import bd.edu.my_class_schedule.data.db.entities.Exam
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {

    @Query("SELECT * FROM exams WHERE id = :id")
    suspend fun getById(id: Long): Exam?

    // Order: non-null date first asc, then non-null start_time asc, then newest id
    @Query("""
        SELECT * FROM exams
        ORDER BY 
            CASE WHEN exam_date IS NULL THEN 1 ELSE 0 END,
            exam_date ASC,
            CASE WHEN start_time IS NULL THEN 1 ELSE 0 END,
            start_time ASC,
            id DESC
    """)
    fun observeAll(): Flow<List<Exam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Exam): Long

    @Update
    suspend fun update(entity: Exam)

    @Delete
    suspend fun delete(entity: Exam)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteById(id: Long)
}