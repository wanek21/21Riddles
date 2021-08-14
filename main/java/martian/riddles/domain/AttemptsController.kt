package martian.riddles.domain

import android.content.SharedPreferences
import martian.riddles.data.local.Data
import martian.riddles.data.local.DataKeys
import martian.riddles.data.local.StoredData
import javax.inject.Inject
import javax.inject.Singleton

class AttemptsController @Inject constructor(
    private val statisticsController: StatisticsController?, // для отправки статистики Firebase
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
) {

    var countWrongAnswers: Int = 0
    var isEndlessAttempts = false
    private val DEFAULT_COUNT_ATTEMPTS = 3

    init {
        countWrongAnswers = sharedPreferences.getInt(DataKeys.COUNT_WRONG_ANSWERS.key, 0)
    }

    fun getCountAttempts(): Int {
        val countAttempts = sharedPreferences.getInt(DataKeys.COUNT_ATTEMPTS.key, DEFAULT_COUNT_ATTEMPTS)
        // если значение в памяти подменили злоумышленики, то возвращаем значение по дефолту
        if(countAttempts > DEFAULT_COUNT_ATTEMPTS) return DEFAULT_COUNT_ATTEMPTS
        return countAttempts
    }

    // сброс попыток (при смене загадки)
    fun resetCountAttempts() {
        editor.putInt(DataKeys.COUNT_ATTEMPTS.key, DEFAULT_COUNT_ATTEMPTS)
        editor.commit()
    }

    // уменьшает кол-во попыток на 1 и сохраняет в памяти
    fun downCountAttempts() {
        val countAttempts = getCountAttempts()
        if (countAttempts > 0) {
            editor.putInt(
                DataKeys.COUNT_ATTEMPTS.key,
                countAttempts - 1
            )
            editor.commit()
        }
    }

    // увеличивает кол-во попыток на 1 и сохраняет в памяти
    fun upCountAttempts() {
        val countAttempts = getCountAttempts()
        if (countAttempts in 0..2) {
            editor.putInt(
                DataKeys.COUNT_ATTEMPTS.key,
                countAttempts + 1
            )
            editor.commit()
        }
        statisticsController?.earnAttempt(1)
    }

    fun upCountWrongAnswers() {
        editor.putInt(DataKeys.COUNT_WRONG_ANSWERS.key, ++countWrongAnswers)
        editor.commit()
        statisticsController?.sendAttempt(isEndlessAttempts)
    }

    // сбросить кол-во неправильных ответов
    fun resetCountWrongAnswers() {
        editor.putInt(DataKeys.COUNT_WRONG_ANSWERS.key, 0)
        editor.commit()
        countWrongAnswers = 0
    }
}