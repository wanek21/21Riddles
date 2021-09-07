package martian.riddles.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import martian.riddles.data.repositories.GameInfoRepository
import martian.riddles.domain.StatisticsController
import martian.riddles.util.Resource
import martian.riddles.util.UpdateType
import javax.inject.Inject

@HiltViewModel
class InfoActivityViewModel @Inject constructor(
    private val statisticsController: StatisticsController,
    private val gameInfoRepository: GameInfoRepository
) : ViewModel() {

    private val _appVersionStatus: MutableLiveData<Resource<UpdateType>> = MutableLiveData()
    val appVersionStatus: LiveData<Resource<UpdateType>> = _appVersionStatus

    fun checkAppVersion() {
        viewModelScope.launch(Dispatchers.IO) {
            _appVersionStatus.postValue(gameInfoRepository.checkAppVersion())
        }
    }

    fun joinGroup() {
        statisticsController.joinGroup()
    }
}