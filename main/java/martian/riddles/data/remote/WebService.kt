package martian.riddles.data.remote

import android.util.Log
import com.skydoves.sandwich.message
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnFailure
import com.skydoves.sandwich.suspendOnSuccess
import martian.riddles.dto.GetRiddle
import martian.riddles.dto.Leaders
import martian.riddles.dto.RegisterUser
import martian.riddles.dto.RegisterUser.StatusCode
import martian.riddles.util.Resource
import javax.inject.Inject

class WebService @Inject constructor(
        private val serverApi: ServerApi
){

    private val failureErrorMessage: String = "Connection error"

    // секретные слова, которые нужно передавать в запросах (+2 к безопасности)
    private val GET_PRIZE_KEY = "chair"

    // регистрация
    suspend fun signUp(registerUser: RegisterUser): Resource<*> {
        var result: Resource<*> = Resource.error("Unknown error", null)

        val response = serverApi.singUp(registerUser)
        response.suspendOnSuccess {
            if(this.data?.resultCode == StatusCode.NICKNAME_IS_ACCEPTED.code) { // успешная регистрация
                result = Resource.success(null)
                Log.d("my","success")
            } // что-то не так
            else if (this.data != null) {
                Log.d("my","some error")
                val resultCode = this.data!!.resultCode

                // сверяем пришедший код статуса с имеющимися в enum
                var signUpStatusCode = StatusCode.values().filter { resultCode == it.code  }
                Log.d("my",signUpStatusCode.size.toString())

                result = Resource.error("", signUpStatusCode[0].message)
            }
        }.suspendOnError {
            Log.d("my","error2")
            result = Resource.error(this.message(),null)
        }.suspendOnFailure {
            Log.d("my","connection error")
            result = Resource.error(failureErrorMessage,null)
        }
        return result
    }

    // получить список лидеров
    suspend fun getLeaders(): Resource<List<Leaders>> {
        var result: Resource<List<Leaders>> = Resource.error("Unknown error", null)

        val response = serverApi.getLeaders("please")
        response.suspendOnSuccess {
            if(this.data != null) {
                Log.d("my", "webService: leaders received")
                result = Resource.success(this.data)
            }
        }.suspendOnError {
            result = Resource.error(this.message(), null)
        }.suspendOnFailure {
            result = Resource.error(failureErrorMessage, null)
        }

        return result
    }

    // получить приз
    suspend fun getPrize(language: String): Resource<String> {
        var result: Resource<String> = Resource.error("Unknown error", null)

        val response = serverApi.getPrize(GET_PRIZE_KEY, language)
        response.suspendOnSuccess {
            if(this.data != null) {
                result = Resource.success(this.data!!.prize)
                Log.d("my","webService prize: ${result.data}")
            }
        }.suspendOnError {
            result = Resource.error(this.message(), null)
        }.suspendOnFailure {
            result = Resource.error(failureErrorMessage, null)
        }

        return result
    }

    suspend fun getRiddle(getRiddle: GetRiddle): Resource<String> {
        var result: Resource<String> = Resource.error("Unknown error", null)


        val response = serverApi.getRiddle(getRiddle)
        response.suspendOnSuccess {
            if(this.data != null) {
                result = Resource.success(this.data!!.riddle)
                Log.d("my","webService riddle: ${result.data}")
            }
        }.suspendOnError {
            result = Resource.error(this.message(), null)
        }.suspendOnFailure {
            result = Resource.error(failureErrorMessage, null)
        }

        return result
    }
}