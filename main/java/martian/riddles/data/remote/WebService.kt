package martian.riddles.data.remote

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.StatusCode
import com.skydoves.sandwich.message
import com.skydoves.sandwich.request
import martian.riddles.dto.RegisterUser
import martian.riddles.util.Resource
import javax.inject.Inject

class WebService @Inject constructor(
        private val serverApi: ServerApi
){

    fun signUp(registerUser: RegisterUser): Resource<*> {
        var result: Resource<*> = Resource.error("Unknown error", null)
        val response = serverApi.singUp(registerUser)?.request { response ->
            result = when (response) {
                is ApiResponse.Success -> {
                    Resource.success(null)
                }
                is ApiResponse.Failure.Error -> { // ошибка на сервере
                    Resource.error(response.message(),null)
                }
                is ApiResponse.Failure.Exception -> { // проблемы с инетом/подключением
                    Resource.error("Connection error",null)
                }
            }
        }
        return result
    }
}