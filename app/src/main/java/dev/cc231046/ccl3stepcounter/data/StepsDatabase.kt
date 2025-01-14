package dev.cc231046.ccl3stepcounter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StepEntity::class, GoalEntity::class], version = 3)
abstract class StepsDatabase : RoomDatabase() {
    abstract fun stepsDao(): StepsDao
    abstract fun goalsDao(): GoalsDao

    companion object{
        @Volatile
        private var Instance: StepsDatabase?= null

        fun getDatabase(context: Context): StepsDatabase{
            return Instance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    StepsDatabase::class.java,
                    "steps_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                Instance = instance
                return instance
            }
        }
    }
}



