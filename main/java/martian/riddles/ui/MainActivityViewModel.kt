package martian.riddles.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import martian.riddles.data.repositories.UsersRepository
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
        usersRepository: UsersRepository)
    : ViewModel() {
}