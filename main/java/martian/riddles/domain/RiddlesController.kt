package martian.riddles.domain

import martian.riddles.data.remote.RequestController.Companion.hasConnection
import martian.riddles.data.repositories.RiddlesRepository
import martian.riddles.data.repositories.UsersRepository
import martian.riddles.dto.CheckAnswer
import martian.riddles.dto.GetRiddle
import martian.riddles.exceptions.NoInternetException
import martian.riddles.util.GetContextClass
import martian.riddles.util.Resource
import martian.riddles.util.Status
import java.util.*
import javax.inject.Inject

class RiddlesController @Inject constructor(
    private val usersRepository: UsersRepository,
    private val riddlesRepository: RiddlesRepository
) {

    private val locale: String

    suspend fun checkAnswer(answer: String): Resource<Boolean> {
        var answer = answer
        answer = answer.trim { it <= ' ' }.lowercase()
        val currentLevel = usersRepository.getMyLevel()
        return if (hasConnection(GetContextClass.getContext())) {
            val isRight: Boolean
            val checkAnswer = CheckAnswer()
            checkAnswer.nickname = usersRepository.getMyNickname()
            checkAnswer.token = usersRepository.getMyToken()
            checkAnswer.answer = answer
            val response = riddlesRepository.checkAnswer(checkAnswer)
            if(response.status == Status.ERROR) Resource.error(response.message!!, null)
            isRight = response.data.toString() == "1"
            if (isRight) {
                riddlesRepository.replaceCurrentRiddle(currentLevel)
            }
            Resource.success(isRight)
        } else throw NoInternetException()
    }

    suspend fun getRiddle(): Resource<String> {
            val level = usersRepository.getMyLevel()
            val token = usersRepository.getMyToken()
            val nickname = usersRepository.getMyNickname()
            val language = Locale.getDefault().language
            var getRiddle = GetRiddle(token, nickname, locale = language, false)

            return riddlesRepository.getCurrentRiddle(getRiddle, level)
        }

    companion object {
        const val DATA_CURRENT_RIDDLE = "current_riddle"
        const val DATA_NEXT_RIDDLE = "next_riddle"
        const val EMPTY_RIDDLE = "empty_riddle"
        const val ERROR_LOAD_RIDDLE = "error_riddle"
    }

    init {
        locale = Locale.getDefault().language
    }
}