package martian.riddles.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Leader::class, Riddles::class, GameInfo::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usersDao(): UsersDao
    abstract fun riddlesDao(): RiddlesDao
    abstract fun gameInfoDao(): GameInfoDao
}