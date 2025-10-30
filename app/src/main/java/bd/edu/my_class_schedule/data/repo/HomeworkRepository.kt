package bd.edu.my_class_schedule.data.repo

import bd.edu.my_class_schedule.data.db.dao.HomeworkDao
import bd.edu.my_class_schedule.data.db.entities.Homework
import kotlinx.coroutines.flow.Flow

class HomeworkRepository(private val dao: HomeworkDao) {

    fun observeAll(): Flow<List<Homework>> = dao.observeAll()

    suspend fun getById(id: Long): Homework? = dao.getById(id)

    suspend fun upsert(entity: Homework) {
        if (entity.id == 0L) {
            dao.insert(entity)
        } else {
            dao.update(entity.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun delete(entity: Homework) = dao.delete(entity)
}