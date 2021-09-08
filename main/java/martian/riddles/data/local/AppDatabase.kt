package martian.riddles.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Leader::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usersDao(): UsersDao
}