package bd.edu.my_class_schedule.data.db.dao

import androidx.room.*
import bd.edu.my_class_schedule.data.db.entities.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY code ASC")
    fun observeAll(): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(subject: Subject): Long

    @Update
    suspend fun update(subject: Subject)

    @Delete
    suspend fun delete(subject: Subject)
}