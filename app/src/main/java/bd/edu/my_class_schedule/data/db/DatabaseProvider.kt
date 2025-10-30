package bd.edu.my_class_schedule.data.db

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "my_class_schedule.db"
            )
                // Dev-only: wipes data when schema version changes and no migration is provided.
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}