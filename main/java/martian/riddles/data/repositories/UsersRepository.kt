package martian.riddles.data.repositories

import martian.riddles.data.local.UsersDao
import javax.inject.Inject

class UsersRepository @Inject constructor(
        usersDao: UsersDao
) {
}