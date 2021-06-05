package martian.riddles.data.local

import androidx.room.Dao
import androidx.room.Query

@Dao
interface GameInfoDao {

    @Query("SELECT prize FROM game_info LIMIT 1")
    fun getPrize(): String

    @Query("UPDATE game_info SET prize=:prize")
    fun setPrize(prize: String)
}