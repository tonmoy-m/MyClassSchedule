package bd.edu.my_class_schedule.data.repo

import bd.edu.my_class_schedule.data.db.dao.SessionDao
import bd.edu.my_class_schedule.data.db.dao.SubjectDao
import bd.edu.my_class_schedule.data.db.dao.TeacherDao
import bd.edu.my_class_schedule.data.db.entities.Session
import bd.edu.my_class_schedule.data.db.entities.Subject
import bd.edu.my_class_schedule.data.db.entities.Teacher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalTime

class TimetableRepository(
    private val sessionDao: SessionDao,
    private val subjectDao: SubjectDao,
    private val teacherDao: TeacherDao
) {
    fun observeSessions(dayOfWeekIso: Int, weekType: Int): Flow<List<Session>> =
        sessionDao.observeByDay(dayOfWeekIso, weekType)

    fun observeSubjects(): Flow<List<Subject>> = subjectDao.observeAll()

    fun observeTeachers(): Flow<List<Teacher>> = teacherDao.observeAll()

    suspend fun saveSession(session: Session): Long = sessionDao.upsert(session)

    suspend fun deleteSession(session: Session) = sessionDao.delete(session)

    /**
     * Suggest defaults for a new session on a given day:
     * - start = lastSessionEnd + 10 minutes (if any), otherwise 09:00
     * - end = start + 60 minutes
     */
    suspend fun suggestDefaultTimes(dayOfWeekIso: Int, weekType: Int): Pair<LocalTime, LocalTime> {
        val sessions = sessionDao.observeByDay(dayOfWeekIso, weekType).first()
        val lastEnd = sessions.maxByOrNull { it.startTime }?.endTime
        val start = lastEnd?.plusMinutes(10) ?: LocalTime.of(9, 0)
        val end = start.plus(Duration.ofMinutes(60))
        return start to end
    }
}