package martian.riddles.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import martian.riddles.data.repositories.UsersRepository
import martian.riddles.domain.AttemptsController
import martian.riddles.domain.RiddlesController
import martian.riddles.util.Resource
import okhttp3.internal.userAgent
import java.util.*
import javax.inject.Inject

@HiltViewModel
class RiddlesActivityViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val riddlesController: RiddlesController,
    private val attemptsController: AttemptsController // контроллер попыток
) : ViewModel() {

    private val _answerStatus: MutableLiveData<Resource<Boolean>> = MutableLiveData()
    val answerStatus: LiveData<Resource<Boolean>> = _answerStatus

    private val _riddle: MutableLiveData<Resource<String>> = MutableLiveData()
    val riddle: LiveData<Resource<String>> = _riddle

    fun getCurrentRiddle() {
        viewModelScope.launch(Dispatchers.IO) {
            _riddle.postValue(riddlesController.getRiddle())
        }
    }

    fun checkAnswer(answer: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _answerStatus.postValue(riddlesController.checkAnswer(answer))
        }
    }

    fun getMyLevel(): Int {
        return usersRepository.getMyLevel()
    }

    fun getCountAttempts(): Int {
        return attemptsController.getCountAttempts()
    }

    fun isEndlessAttempts(): Boolean {
        return attemptsController.isEndlessAttempts
    }

    fun setEndlessAttempts() {
        attemptsController.isEndlessAttempts = true
    }

    fun upCountAttempts() {
        attemptsController.upCountAttempts()
    }

    fun getCountWrongAnswers(): Int {
        return attemptsController.getCountWrongAnswers()
    }

    fun getCountPurchaseOffer(): Int {
        return usersRepository.getCountPurchaseOffer()
    }

    fun changeCountPurchaseOffer(count: Int) {
        usersRepository.changeCountPurchaseOffer(count)
    }
}