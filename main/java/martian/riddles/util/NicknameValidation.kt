package martian.riddles.util

import martian.riddles.R

enum class NicknameValidation(val errorMessage: Int) {
    BAD_WORDS (R.string.bad_words),
    WRONG_SYMBOLS (R.string.wrong_symbols),
    SHORT_NICKNAME (R.string.invalid_long_name),
    LONG_NICKNAME (R.string.invalid_long_name),
    MANY_SPACES (R.string.many_scapes), // больше одного пробела
    NICKNAME_IS_ACCEPTED (0)
    /*SERVER_DOES_NOT_WORKING,
    UNKNOWN_ERROR,
    NO_INTERNET,
    SERVER_ERROR*/
}