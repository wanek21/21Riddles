package martian.riddles.dto

data class GetRiddle (
    var token: String? = null,
    var nickname: String? = null,
    var locale: String? = null, // язык
    var isNext: Boolean = false
)