package martian.riddles.data.remote

import android.util.Log
import com.skydoves.sandwich.message
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnFailure
import com.skydoves.sandwich.suspendOnSuccess
import martian.riddles.dto.RegisterUser
import martian.riddles.dto.RegisterUser.StatusCode
import martian.riddles.util.Resource
import javax.inject.Inject

class WebService @Inject constructor(
        private val serverApi: ServerApi
){

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
            result = Resource.error("Connection error",null)
        }
        return result
    }
}