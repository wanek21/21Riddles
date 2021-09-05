package martian.riddles.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import martian.riddles.data.repositories.UsersRepository
import martian.riddles.dto.RegisterUser
import martian.riddles.util.NicknameValidation
import martian.riddles.util.Resource
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val TOKEN_LENGTH = 42

    private val _registerStatus: MutableLiveData<Resource<*>> = MutableLiveData()
    val registerStatus: LiveData<Resource<*>> = _registerStatus

    fun signUp(nickname: String) {
        _registerStatus.value = Resource.loading(null)

        var nick = nickname
        nick = nick.trim(' ') // обрезаем пробелы в начале и конце
        val nicknameValid = isNicknameValid(nick)
        if (nicknameValid != NicknameValidation.NICKNAME_IS_ACCEPTED) { // если ник не прошел валидацию
            _registerStatus.value = Resource.error(nicknameValid.errorMessage, null)
        } else {
            val token = generateRandomHexString(TOKEN_LENGTH)
            val registerUser = RegisterUser(
                nickname = "$nick;wins4", // добавляем соль к концу ника (+2 к безопасности)
                uniqueString = "", // уникальный id устройства, уже не нужен, но на бэке остался
                token = token
            )
            viewModelScope.launch {
                _registerStatus.value = usersRepository.signUp(registerUser)
            }
        }
    }

    /* генерация токена (случайный набор цифр и букв)
    * в теории у кого то могут сгенерироваться одинаковые токены, но
    * это очень маловероятно и не особо страшно.
    */
    private fun generateRandomHexString(length: Int): String {
        val r = Random()
        val sb = StringBuffer()
        while (sb.length < length) {
            sb.append(Integer.toHexString(r.nextInt()))
        }
        return sb.toString().substring(0, length)
    }

    // проверка ника на валидность
    private fun isNicknameValid(nickname: String): NicknameValidation {
        if (nickname.length < 4) return NicknameValidation.SHORT_NICKNAME
        if (nickname.length > 15) return NicknameValidation.LONG_NICKNAME
        val lowNickname = nickname.toLowerCase()
        if (lowNickname.contains("пизда") ||
            lowNickname.contains("fuck") ||
            lowNickname.contains("член") ||
            lowNickname.contains("пидор") ||
            lowNickname.contains("пидр") ||
            lowNickname.contains("pidor") ||
            lowNickname.contains("соси") ||
            lowNickname.contains("sosi") ||
            lowNickname.contains("pizda") ||
            lowNickname.contains("pizdec") ||
            lowNickname.contains("pidr")
        ) {
            return NicknameValidation.BAD_WORDS
        }
        if (nickname.indexOf(' ') != nickname.lastIndexOf(' ')) return NicknameValidation.MANY_SPACES // если больше одного пробела
        return if (nickname.matches(Regex("[A-Za-z0-9а-яА-Я\\s]+"))) NicknameValidation.NICKNAME_IS_ACCEPTED
        else NicknameValidation.WRONG_SYMBOLS
    }
}