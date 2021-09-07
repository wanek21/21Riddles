package martian.riddles.domain

import martian.riddles.data.repositories.UsersRepository
import javax.inject.Inject

class AttemptsController @Inject constructor(
    private val usersRepository: UsersRepository,
    private val statisticsController: StatisticsController?, // для отправки статистики Firebase
) {

    var isEndlessAttempts = false

    fun getCountAttempts(): Int {
        val countAttempts = usersRepository.getMyCountAttempts()
        // если значение в памяти подменили злоумышленики, то возвращаем значение по дефолту
        if(countAttempts > DEFAULT_COUNT_ATTEMPTS) return DEFAULT_COUNT_ATTEMPTS
        return countAttempts
    }

    // сброс попыток (при смене загадки)
    fun resetCountAttempts() {
        usersRepository.changeMyCountAttempts(DEFAULT_COUNT_ATTEMPTS)
    }

    // уменьшает кол-во попыток на 1 и сохраняет в памяти
    fun downCountAttempts() {
        val countAttempts = getCountAttempts()
        if (countAttempts > 0) {
            usersRepository.changeMyCountAttempts(countAttempts - 1)
        }
    }

    // увеличивает кол-во попыток на 1 и сохраняет в памяти
    fun upCountAttempts() {
        val countAttempts = getCountAttempts()
        if (countAttempts in 0..2) {
            usersRepository.changeMyCountAttempts(countAttempts + 1)
        }
        statisticsController?.earnAttempt(1)
    }

    fun upCountWrongAnswers() {
        val currentCountWrongAnswers = usersRepository.getMyCountWrongAnswers()
        usersRepository.changeMyCountWrongAnswers(currentCountWrongAnswers+1)
        statisticsController?.sendAttempt(isEndlessAttempts)
    }

    fun getCountWrongAnswers(): Int {
        return usersRepository.getMyCountWrongAnswers()
    }

    // сбросить кол-во неправильных ответов
    fun resetCountWrongAnswers() {
        usersRepository.changeMyCountWrongAnswers(0)
    }

    companion object {
        const val DEFAULT_COUNT_ATTEMPTS = 3
    }
}