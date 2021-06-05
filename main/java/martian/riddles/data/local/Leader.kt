package martian.riddles.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaders")
data class Leader(
        @PrimaryKey val id: Int, // место в рейтинге (от 1 до 4, может расширяться)
        @ColumnInfo(name = "nickname") val nickname: String,
        @ColumnInfo(name = "level") val level: Int,
        @ColumnInfo(name = "count_users_on_this_level") val countUsersOnLevel: Int,
        @ColumnInfo(name = "complete_game") val completeGame: Boolean
)
