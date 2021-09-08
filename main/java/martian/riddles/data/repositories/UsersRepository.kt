package martian.riddles.data.repositories

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import martian.riddles.data.local.DataKeys
import martian.riddles.data.local.UsersDao
import martian.riddles.data.remote.WebService
import martian.riddles.domain.AttemptsController
import martian.riddles.dto.GetEmail
import martian.riddles.dto.GetPlace
import martian.riddles.dto.Leaders
import martian.riddles.dto.RegisterUser
import martian.riddles.util.Resource
import martian.riddles.util.Status
import martian.riddles.util.log
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class UsersRepository @Inject constructor(
    private val usersDao: UsersDao,
    private val sharedPreferences: SharedPreferences,
    private val editSharedPreferences: SharedPreferences.Editor,
    private val webService: WebService
) {

    suspend fun signUp(registerUser: RegisterUser): Resource<*> {
        return withContext(Dispatchers.IO) {
            val result = webService.signUp(registerUser)

            // если регистрация успешна, сохраняем данные об игроке на устройство
            if(result.status == Status.SUCCESS) {
                editSharedPreferences.putString(DataKeys.NICKNAME.key, registerUser.nickname.dropLast(6)) // обрезаем "соль" в конце ника, которая была нужна для бэка
                editSharedPreferences.putInt(DataKeys.LEVEL.key, 1)
                editSharedPreferences.putString(DataKeys.TOKEN.key, registerUser.token)
                editSharedPreferences.commit()
            }

            result
        }
    }

    suspend fun getLeaders(): Resource<ArrayList<Leaders>> {
        return withContext(Dispatchers.IO) {
            //log( "before dao getting leaders")
            val leadersDao = usersDao.getLeaders()
            //log( "after dao getting leaders")
            refreshLeaders()
            Resource.success(Leaders.fromDao(leadersDao))
        }
    }

    // получить место при завершении игры
    suspend fun getMyPlace(): Resource<Int> {
        return withContext(Dispatchers.IO) {
            val getPlace = GetPlace(getMyNickname(), getMyToken())
            webService.getPlace(getPlace)
        }
    }

    suspend fun getEmailContact(): Resource<String> {
        return withContext(Dispatchers.IO) {
            val getEmail = GetEmail(getMyNickname(), getMyToken(), Locale.getDefault().language)
            webService.getEmailContact(getEmail)
        }
    }

    private suspend fun refreshLeaders() {
        val leaders = webService.getLeaders()
        if(leaders.status == Status.SUCCESS) {
            val leadersDao = Leaders.toDao(leaders.data)
            usersDao.saveLeaders(leadersDao)
        }
    }

    fun isLogged(): Boolean {
        val nickname = sharedPreferences.getString(DataKeys.NICKNAME.key,"")
        return (nickname?.isNotEmpty() == true)
    }

    fun getMyNickname(): String {
        val nickname = sharedPreferences.getString(DataKeys.NICKNAME.key, "")
        return nickname ?: ""
    }

    fun getMyLevel(): Int {
        return sharedPreferences.getInt(DataKeys.LEVEL.key, 1)
    }

    fun getMyToken(): String {
        return sharedPreferences.getString(DataKeys.TOKEN.key, "") ?: ""
    }

    fun upMyLevel() { // увеличить уровень на 1
        val currentLevel = sharedPreferences.getInt(DataKeys.LEVEL.key, 1)
        editSharedPreferences.putInt(DataKeys.LEVEL.key, (currentLevel+1))
        editSharedPreferences.commit()
    }

    fun getMyCountAttempts(): Int {
        return sharedPreferences.getInt(DataKeys.COUNT_ATTEMPTS.key, AttemptsController.DEFAULT_COUNT_ATTEMPTS)
    }

    fun changeMyCountAttempts(count: Int) {
        editSharedPreferences.putInt(DataKeys.COUNT_ATTEMPTS.key, count)
        editSharedPreferences.commit()
    }

    fun getMyCountWrongAnswers(): Int {
        return sharedPreferences.getInt(DataKeys.COUNT_WRONG_ANSWERS.key, 0)
    }

    fun changeMyCountWrongAnswers(count: Int) {
        editSharedPreferences.putInt(DataKeys.COUNT_WRONG_ANSWERS.key, count)
        editSharedPreferences.commit()
    }

    fun wasCompleteGameAnimation(): Boolean {
        return sharedPreferences.getBoolean(DataKeys.DONE_GAME_ANIM_COMPLETE.key, false)
    }

    fun completeGameAnimation() {
        editSharedPreferences.putBoolean(DataKeys.DONE_GAME_ANIM_COMPLETE.key, true)
        editSharedPreferences.commit()
    }

    fun getCountPurchaseOffer(): Int {
        return sharedPreferences.getInt(DataKeys.SHOW_PURCHASE_COUNT.key, 0)
    }

    fun changeCountPurchaseOffer(count: Int) {
        editSharedPreferences.putInt(DataKeys.SHOW_PURCHASE_COUNT.key, count)
        editSharedPreferences.commit()
    }
}