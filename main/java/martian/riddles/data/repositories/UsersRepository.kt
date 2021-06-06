package martian.riddles.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import martian.riddles.data.local.UsersDao
import martian.riddles.data.remote.ServerApi
import martian.riddles.data.remote.WebService
import martian.riddles.dto.RegisterUser
import martian.riddles.util.Resource
import javax.inject.Inject

class UsersRepository @Inject constructor(
        private val usersDao: UsersDao,
        private val webService: WebService
) {

    suspend fun registerUser(registerUser: RegisterUser): Resource<*> {
        return withContext(Dispatchers.IO) {
            webService.signUp(registerUser)
        }
    }
}