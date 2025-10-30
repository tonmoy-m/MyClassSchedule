package bd.edu.my_class_schedule.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import bd.edu.my_class_schedule.data.db.dao.ExamDao
import bd.edu.my_class_schedule.data.db.dao.HomeworkDao
import bd.edu.my_class_schedule.data.db.dao.SessionDao
import bd.edu.my_class_schedule.data.db.dao.SubjectDao
import bd.edu.my_class_schedule.data.db.dao.TeacherDao
import bd.edu.my_class_schedule.data.db.entities.Exam
import bd.edu.my_class_schedule.data.db.entities.Homework
import bd.edu.my_class_schedule.data.db.entities.Session
import bd.edu.my_class_schedule.data.db.entities.Subject
import bd.edu.my_class_schedule.data.db.entities.Teacher

@Database(
    entities = [
        Subject::class,
        Teacher::class,
        Session::class,
        Exam::class,
        Homework::class
    ],
    version = 6, // bump +1 from your current version
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun teacherDao(): TeacherDao
    abstract fun sessionDao(): SessionDao
    abstract fun examDao(): ExamDao
    abstract fun homeworkDao(): HomeworkDao
}