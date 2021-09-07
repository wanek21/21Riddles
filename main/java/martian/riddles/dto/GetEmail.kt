package martian.riddles.dto

data class GetEmail (
    var nickname: String? = null,
    var token: String? = null,
    var locale: String? = null
)