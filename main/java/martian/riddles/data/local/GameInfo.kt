package martian.riddles.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_info")
data class GameInfo(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "prize") val prize: String
)
