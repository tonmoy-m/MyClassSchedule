package bd.edu.my_class_schedule.data.db.dao

import androidx.room.*
import bd.edu.my_class_schedule.data.db.entities.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("""
        SELECT * FROM sessions
        WHERE day_of_week = :day
          AND (week_type = 0 OR week_type = :weekType)
        ORDER BY start_time
    """)
    fun observeByDay(day: Int, weekType: Int): Flow<List<Session>>

    @Query("SELECT * FROM sessions ORDER BY day_of_week, start_time")
    fun observeAll(): Flow<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: Session): Long

    @Delete
    suspend fun delete(session: Session)
}