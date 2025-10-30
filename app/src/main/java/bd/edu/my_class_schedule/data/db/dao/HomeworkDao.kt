package bd.edu.my_class_schedule.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import bd.edu.my_class_schedule.data.db.entities.Homework
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {

    @Query("SELECT * FROM homework ORDER BY isCompleted ASC, CASE WHEN dueDateMillis IS NULL THEN 1 ELSE 0 END, dueDateMillis ASC, createdAt DESC")
    fun observeAll(): Flow<List<Homework>>

    @Query("SELECT * FROM homework WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Homework?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Homework): Long

    @Update
    suspend fun update(entity: Homework)

    @Delete
    suspend fun delete(entity: Homework)
}