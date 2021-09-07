package martian.riddles.domain

import martian.riddles.data.repositories.RiddlesRepository
import martian.riddles.data.repositories.UsersRepository
import martian.riddles.dto.CheckAnswer
import martian.riddles.dto.GetRiddle
import martian.riddles.util.Resource
import martian.riddles.util.Status
import java.util.*
import javax.inject.Inject

class RiddlesController @Inject constructor(
    private val usersRepository: UsersRepository,
    private val riddlesRepository: RiddlesRepository,
    private val attemptsController: AttemptsController
) {

    suspend fun checkAnswer(asw: String): Resource<Boolean> {
        val answer = asw.trim { it <= ' ' }.lowercase()
        val currentLevel = usersRepository.getMyLevel()
        val isAnswerRight: Boolean
        val checkAnswer = CheckAnswer()
        checkAnswer.nickname = usersRepository.getMyNickname()
        checkAnswer.token = usersRepository.getMyToken()
        checkAnswer.answer = answer

        val response = riddlesRepository.checkAnswer(checkAnswer)
        if(response.status == Status.ERROR)
            return Resource.error(response.message!!, null)

        // бэк возвращает либо 0 либо 1
        isAnswerRight = response.data.toString() == "1"
        if (isAnswerRight) {
            attemptsController.resetCountAttempts()
            attemptsController.resetCountWrongAnswers()
            usersRepository.upMyLevel()
            riddlesRepository.replaceCurrentRiddle(currentLevel)
        } else {
            val countAttempts = attemptsController.getCountAttempts()
            if (countAttempts > 0 && !attemptsController.isEndlessAttempts) {
                attemptsController.downCountAttempts()
            }
            attemptsController.upCountWrongAnswers()
        }
        return Resource.success(isAnswerRight)
    }

    suspend fun getRiddle(): Resource<String> {
            val level = usersRepository.getMyLevel()
            val token = usersRepository.getMyToken()
            val nickname = usersRepository.getMyNickname()
            val language = Locale.getDefault().language
            val getRiddle = GetRiddle(token, nickname, locale = language, false)

            return riddlesRepository.getCurrentRiddle(getRiddle, level)
        }

    companion object {
        const val DATA_CURRENT_RIDDLE = "current_riddle"
        const val DATA_NEXT_RIDDLE = "next_riddle"
        const val EMPTY_RIDDLE = "empty_riddle"
        const val ERROR_LOAD_RIDDLE = "error_riddle"
    }

}