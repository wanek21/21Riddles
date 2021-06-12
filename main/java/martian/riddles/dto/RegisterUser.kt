package martian.riddles.dto

import martian.riddles.R

data class RegisterUser(
        val nickname: String,
        val uniqueString: String,
        val token: String
) {
    enum class StatusCode(val code: Int, val message: Int) {
        MANY_SIGN_UP_IP(4, R.string.many_ip_logup),
        NICKNAME_IS_TAKEN(2, R.string.login_exist),
        INVALID_NICKNAME(3, R.string.wrong_symbols),
        NICKNAME_IS_ACCEPTED(1, -1),
        UNKNOWN_STATUS(-1, -1)
    }
}
