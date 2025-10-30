package bd.edu.my_class_schedule.data.repo

import bd.edu.my_class_schedule.data.db.dao.ExamDao
import bd.edu.my_class_schedule.data.db.entities.Exam
import kotlinx.coroutines.flow.Flow

class ExamRepository(private val dao: ExamDao) {
    fun observeAll(): Flow<List<Exam>> = dao.observeAll()
    suspend fun getById(id: Long): Exam? = dao.getById(id)

    suspend fun upsert(entity: Exam): Long =
        if (entity.id == 0L) dao.insert(entity) else {
            dao.update(entity); entity.id
        }

    suspend fun delete(entity: Exam) = dao.delete(entity)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
}