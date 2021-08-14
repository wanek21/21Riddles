package martian.riddles.data.repositories

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import martian.riddles.R
import martian.riddles.data.local.Data
import martian.riddles.data.local.DataKeys
import martian.riddles.data.local.StoredData
import martian.riddles.data.remote.WebService
import martian.riddles.domain.RiddlesController
import martian.riddles.dto.GetRiddle
import martian.riddles.dto.Player
import martian.riddles.exceptions.NoInternetException
import martian.riddles.util.GetContextClass
import martian.riddles.util.Resource
import martian.riddles.util.Status
import org.intellij.lang.annotations.Language
import javax.inject.Inject

class RiddlesRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val webService: WebService,
    @ApplicationContext private val context: Context
) {

    private val START_REMOTE_RIDDLES_LEVEL = 10 // с этого уровня загадки загружаются с сервера

    suspend fun getCurrentRiddle(getRiddle: GetRiddle, currentLevel: Int): Resource<String> {
        return withContext(Dispatchers.IO) {
            var riddle = ""
            when (currentLevel) {
                1 -> riddle = context.resources.getString(R.string.qst1)
                2 -> riddle = context.resources.getString(R.string.qst2)
                3 -> riddle = context.resources.getString(R.string.qst3)
                4 -> riddle = context.resources.getString(R.string.qst4)
                5 -> riddle = context.resources.getString(R.string.qst5)
                6 -> riddle = context.resources.getString(R.string.qst6)
                7 -> riddle = context.resources.getString(R.string.qst7)
                8 -> riddle = context.resources.getString(R.string.qst8)
                9 -> riddle = context.resources.getString(R.string.qst9)
                in 10..21 -> {
                    // достаем загадку из памяти
                    riddle = sharedPreferences.getString(
                        DataKeys.CURRENT_RIDDLE.key,
                        DataKeys.EMPTY_RIDDLE.key
                    ) ?: riddle

                    // если загадки в памяти еще нет или была ошибка при загрузке, загружаем
                    if (riddle == DataKeys.EMPTY_RIDDLE.key || riddle == DataKeys.ERROR_LOAD_RIDDLE.key) {
                        riddle = context.getString(R.string.load_riddle_error)
                        loadRiddleFromServer(getRiddle)
                    }
                }
            }

            // подгружаем следующую загадку
            if (currentLevel >= START_REMOTE_RIDDLES_LEVEL - 1 && currentLevel != 21) {
                // если еще не загружена
                if (sharedPreferences.getString(
                        DataKeys.NEXT_RIDDLE.key,
                        DataKeys.EMPTY_RIDDLE.key
                    ) == DataKeys.EMPTY_RIDDLE.key
                ) {
                    loadRiddleFromServer(getRiddle)
                }
            }

            Resource.success(riddle)
        }
    }

    private suspend fun loadRiddleFromServer(getRiddle: GetRiddle) {
        val riddle = webService.getRiddle(getRiddle)

        if (riddle.status == Status.SUCCESS)
            editor.putString(
                if(getRiddle.isNext) DataKeys.NEXT_RIDDLE.key else DataKeys.CURRENT_RIDDLE.key,
                riddle.data
            )
        else
            editor.putString(DataKeys.NEXT_RIDDLE.key, DataKeys.ERROR_LOAD_RIDDLE.key)

        editor.commit()
    }

    fun checkAnswer(answer: String) {

    }
}