package martian.riddles.dto

data class DataOfUser(
        var nickname: String = "",
        var level: Int = 1,
        var uniqueString: String? = null,
        var token: String? = null
) {

}