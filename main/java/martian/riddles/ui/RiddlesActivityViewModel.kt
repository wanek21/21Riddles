package martian.riddles.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import martian.riddles.data.repositories.RiddlesRepository
import martian.riddles.data.repositories.UsersRepository
import martian.riddles.domain.AttemptsController
import martian.riddles.domain.PurchaseController
import martian.riddles.dto.GetRiddle
import martian.riddles.util.Resource
import java.util.*
import javax.inject.Inject

@HiltViewModel
class RiddlesActivityViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val riddlesRepository: RiddlesRepository,
    private val purchaseController: PurchaseController, // для работы с покупкой беск. попыток
    private val attemptsController: AttemptsController // контроллер попыток
) : ViewModel() {

    private val _answerStatus: MutableLiveData<Resource<Int>> = MutableLiveData()
    val answerStatus: LiveData<Resource<Int>> = _answerStatus

    init {

    }

    fun getCurrentRiddle() {
        viewModelScope.launch(Dispatchers.IO) {
            val language = Locale.getDefault().language
            val token = usersRepository.getMyToken()
            val nickname = usersRepository.getMyNickname()
            val level = usersRepository.getMyLevel()
            var getRiddle = GetRiddle(token, nickname, locale = language, false)
            riddlesRepository.getCurrentRiddle(getRiddle, currentLevel = level).data ?: "Error"
        }
    }

    fun checkAnswer() {

    }

}