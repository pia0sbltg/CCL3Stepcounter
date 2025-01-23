package dev.cc231046.ccl3stepcounter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [StepEntity::class, GoalEntity::class, PetEntity::class, OwnedPetsEntity::class], version = 9)
abstract class StepsDatabase : RoomDatabase() {
    abstract fun stepsDao(): StepsDao
    abstract fun goalsDao(): GoalsDao
    abstract fun petDao(): PetDao
    abstract fun ownedPetsDao(): OwnedPetsDao

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



