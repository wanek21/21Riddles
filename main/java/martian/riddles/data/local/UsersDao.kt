package martian.riddles.data.local

import androidx.room.*

@Dao
interface UsersDao {

    @Query("SELECT * FROM leaders")
    suspend fun getLeaders(): List<Leader>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLeaders(leaders: List<Leader>)
}