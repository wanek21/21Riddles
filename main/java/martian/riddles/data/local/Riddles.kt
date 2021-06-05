package martian.riddles.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "riddles")
data class Riddles(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "current_riddle") val currentRiddle: String?,
        @ColumnInfo(name = "next_riddle") val nextRiddle: String?,
) {
    enum class Status {
        EMPTY,
        ERROR_LOADING
    }
}
