package martian.riddles.data.local

import androidx.room.*

@Dao
interface UsersDao {

    // данные о других игроках

    @Query("SELECT * FROM leaders ORDER BY level")
    fun getLeaders(): List<Leader>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setLeader(leader: Leader)
}