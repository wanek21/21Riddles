package martian.riddles.data.repositories

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import martian.riddles.data.local.DataKey
import martian.riddles.data.local.UsersDao
import martian.riddles.data.remote.WebService
import martian.riddles.dto.RegisterUser
import martian.riddles.util.Resource
import martian.riddles.util.Status
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
                editSharedPreferences.putString(DataKey.NICKNAME.key, registerUser.nickname.dropLast(6)) // обрезаем "соль" в конце ника, которая нужна на бэке для безопасности
                editSharedPreferences.putInt(DataKey.LEVEL.key, 1)
                editSharedPreferences.putString(DataKey.TOKEN.key, registerUser.token)
                editSharedPreferences.commit()
            }

            result
        }
    }

    fun getMyNickname(): String {
        val nickname = sharedPreferences.getString(DataKey.NICKNAME.key, "")
        return nickname ?: ""
    }

    fun getMyLevel(): Int {
        return sharedPreferences.getInt(DataKey.LEVEL.key, 0)
    }

    fun upMyLevel() { // увеличить уровень на 1
        val currentLevel = sharedPreferences.getInt(DataKey.LEVEL.key, 0)
        editSharedPreferences.putInt(DataKey.LEVEL.key, (currentLevel+1))
        editSharedPreferences.commit()
    }

    fun isLogged(): Boolean {
        val nickname = sharedPreferences.getString(DataKey.NICKNAME.key,"")
        Log.d("my", "nick check: $nickname")
        return (nickname?.isNotEmpty() == true)
    }
}