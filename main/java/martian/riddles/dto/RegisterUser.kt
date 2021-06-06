package martian.riddles.dto

data class RegisterUser(
        val nickname: String,
        val uniqueString: String,
        val token: String
)
