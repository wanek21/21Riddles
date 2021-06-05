package martian.riddles.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "nickname") val nickname: String,
        @ColumnInfo(name = "level") val level: Int,
        @ColumnInfo(name = "token") val token: String?
)
