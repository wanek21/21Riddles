package martian.riddles.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import martian.riddles.data.repositories.GameInfoRepository
import martian.riddles.data.repositories.UsersRepository
import martian.riddles.dto.Leaders
import martian.riddles.util.Resource
import martian.riddles.util.SingleLiveEvent
import martian.riddles.util.Status
import martian.riddles.util.log
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val gameInfoRepository: GameInfoRepository
) : ViewModel() {

    private val _leaders: MutableLiveData<Resource<ArrayList<Leaders>>> = MutableLiveData()
    val leaders: LiveData<Resource<ArrayList<Leaders>>> = _leaders

    private val _prize: MutableLiveData<Resource<String>> = MutableLiveData()
    val prize: LiveData<Resource<String>> = _prize

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repeat(1000) {
                log("repeat leaders update")
                val leaders = usersRepository.getLeaders()
                //log("received leaders")
                if(leaders.status == Status.SUCCESS) {
                    //log( "leaders received successfully")
                    _leaders.value = leaders
                } else {
                    //log("error leaders")
                }

                _prize.value = gameInfoRepository.getPrize(Locale.getDefault().language)
                delay(5000L)
            }
        }
    }

    fun isLogged(): Boolean {
        return usersRepository.isLogged()
    }

    fun getNickname(): String {
        return usersRepository.getMyNickname()
    }

    fun getLevel(): Int {
        return usersRepository.getMyLevel()
    }

    fun upCountLaunchApp() {
        gameInfoRepository.upCountLaunchApp()
    }

    fun getCountCountLaunch(): Int {
        return gameInfoRepository.getCountLaunchApp()
    }
}