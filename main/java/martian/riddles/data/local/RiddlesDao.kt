package martian.riddles.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RiddlesDao {

    @Query("SELECT current_riddle FROM riddles LIMIT 1")
    fun getCurrentRiddle(): String

    @Query("SELECT next_riddle FROM riddles LIMIT 1")
    fun getNextRiddle(): String

    @Query("UPDATE riddles SET current_riddle=:riddle")
    fun setCurrentRiddle(riddle: String)

    @Query("UPDATE riddles SET next_riddle=:riddle")
    fun setNextRiddle(riddle: String)
}