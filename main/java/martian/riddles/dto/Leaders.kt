package martian.riddles.dto

data class Leaders(
        val riddle: Int = 0,
        val nickname: String? = null,
        val countUsersOnThisRiddle: Int = 0,
        val isCompleteGame: Boolean = false
)