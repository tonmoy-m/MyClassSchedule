package bd.edu.my_class_schedule.data.repo

import bd.edu.my_class_schedule.data.db.dao.TeacherDao
import bd.edu.my_class_schedule.data.db.entities.Teacher
import kotlinx.coroutines.flow.Flow

class TeacherRepository(private val dao: TeacherDao) {
    fun observeAll(): Flow<List<Teacher>> = dao.observeAll()
    suspend fun getById(id: Long): Teacher? = dao.getById(id)
    suspend fun upsert(teacher: Teacher): Long = dao.upsert(teacher)
    suspend fun delete(teacher: Teacher) = dao.delete(teacher)
}