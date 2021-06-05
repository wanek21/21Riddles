package martian.riddles.data.repositories

import martian.riddles.data.local.RiddlesDao
import javax.inject.Inject

class RiddlesRepository @Inject constructor(
        riddlesDao: RiddlesDao
) {
}