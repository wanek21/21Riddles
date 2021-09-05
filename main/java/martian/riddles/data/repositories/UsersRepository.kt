package martian.riddles.data.repositories

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import martian.riddles.data.local.DataKeys
import martian.riddles.data.local.UsersDao
import martian.riddles.data.remote.WebService
import martian.riddles.dto.Leaders
import martian.riddles.dto.RegisterUser
import martian.riddles.util.Resource
import martian.riddles.util.Status
import martian.riddles.util.log
import javax.inject.Inject

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

    fun getMyNickname(): String {
        val nickname = sharedPreferences.getString(DataKeys.NICKNAME.key, "")
        return nickname ?: ""
    }

    fun getMyLevel(): Int {
        return sharedPreferences.getInt(DataKeys.LEVEL.key, 0)
    }

    fun getMyToken(): String {
        return sharedPreferences.getString(DataKeys.TOKEN.key, "") ?: ""
    }

    fun upMyLevel() { // увеличить уровень на 1
        val currentLevel = sharedPreferences.getInt(DataKeys.LEVEL.key, 0)
        editSharedPreferences.putInt(DataKeys.LEVEL.key, (currentLevel+1))
        editSharedPreferences.commit()
    }

    suspend fun getLeaders(): Resource<ArrayList<Leaders>> {
        return withContext(Dispatchers.IO) {
            log( "before dao getting leaders")
            val leadersDao = usersDao.getLeaders()
            log( "after dao getting leaders")
            refreshLeaders()
            Resource.success(Leaders.fromDao(leadersDao))
        }
    }

    private suspend fun refreshLeaders() {
        val leaders = webService.getLeaders()
        if(leaders.status == Status.SUCCESS) {
            log( "before transform")
            val leadersDao = Leaders.toDao(leaders.data)
            usersDao.saveLeaders(leadersDao)
            log( "after dao saving")
        }
    }

    fun isLogged(): Boolean {
        val nickname = sharedPreferences.getString(DataKeys.NICKNAME.key,"")
        //log( "nick check: $nickname")
        return (nickname?.isNotEmpty() == true)
    }
}