package martian.riddles.data.local

import androidx.room.*

@Dao
interface UsersDao {

    // личные данные

    @Query("SELECT * FROM user WHERE id=1")
    fun getUser(): User

    @Query("SELECT level FROM user WHERE id=1")
    fun getMyLevel(): Int

    @Query("UPDATE user SET level=:level WHERE id=1")
    fun updateMyLevel(level: Int)



    // данные о других игроках

    @Query("SELECT * FROM leaders ORDER BY level")
    fun getLeaders(): List<Leader>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setLeader(leader: Leader)
}