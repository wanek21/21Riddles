package martian.riddles.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import martian.riddles.data.repositories.UsersRepository
import martian.riddles.util.Resource
import javax.inject.Inject

@HiltViewModel
class DoneActivityViewModel @Inject constructor(
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _place: MutableLiveData<Resource<Int>> = MutableLiveData()
    val place: LiveData<Resource<Int>> = _place

    private val _emailContact: MutableLiveData<Resource<String>> = MutableLiveData()
    val emailContact: LiveData<Resource<String>> = _emailContact

    fun getPlace() {
        viewModelScope.launch(Dispatchers.IO) {
            _place.postValue(usersRepository.getMyPlace())
        }
    }

    fun getMyLevel(): Int {
        return usersRepository.getMyLevel()
    }

    fun getEmailContact() {
        viewModelScope.launch(Dispatchers.IO) {
            _emailContact.postValue(usersRepository.getEmailContact())
        }
    }
}