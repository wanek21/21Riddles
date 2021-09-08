package martian.riddles.data.remote

import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnFailure
import com.skydoves.sandwich.suspendOnSuccess
import martian.riddles.R
import martian.riddles.dto.*
import martian.riddles.dto.RegisterUser.StatusCode
import martian.riddles.util.Resource
import martian.riddles.util.UpdateType
import martian.riddles.util.log
import javax.inject.Inject

class WebService @Inject constructor(
        private val serverApi: ServerApi
){

    // секретные слова, которые нужно передавать в запросах (+2 к безопасности)
    private val GET_PRIZE_KEY = "chair"

    // регистрация
    suspend fun signUp(registerUser: RegisterUser): Resource<*> {
        var result: Resource<*> = Resource.error(R.string.unknown_error, null)

        val response = serverApi.singUp(registerUser)
        with(response) {
            suspendOnSuccess {
                if (this.data?.resultCode == StatusCode.NICKNAME_IS_ACCEPTED.code) { // успешная регистрация
                    result = Resource.success(null)
                    log("success")
                } else if (this.data != null) { // что-то не так
                    log("some error")
                    val resultCode = this.data!!.resultCode

                    // сверяем пришедший код статуса с имеющимися в enum
                    var signUpStatusCode = StatusCode.values().filter { resultCode == it.code }
                    log(signUpStatusCode.size.toString())
                    log("signUp error: $resultCode")

                    result = Resource.error(signUpStatusCode[0].message, null)
                }
            }
            suspendOnError {
                log("signUp: error on server")
                result = Resource.error(R.string.error_on_server, null)
            }
            suspendOnFailure {
                log("connection error")
                result = Resource.error(R.string.connection_error, null)
            }
        }
        return result
    }

    // получить список лидеров
    suspend fun getLeaders(): Resource<List<Leaders>> {
        var result: Resource<List<Leaders>> = Resource.error(R.string.unknown_error, null)

        val response = serverApi.getLeaders("please")
        with(response) {
            suspendOnSuccess {
                if (this.data != null) {
                    //log("webService: leaders received")
                    result = Resource.success(this.data)
                }
            }
            suspendOnError { result = Resource.error(R.string.error_on_server, null) }
            suspendOnFailure { result = Resource.error(R.string.connection_error, null) }
        }

        return result
    }

    // получить приз
    suspend fun getPrize(language: String): Resource<String> {
        var result: Resource<String> = Resource.error(R.string.unknown_error, null)

        val response = serverApi.getPrize(GET_PRIZE_KEY, language)
        with(response) {
            suspendOnSuccess {
                if (this.data != null) {
                    result = Resource.success(this.data!!.prize)
                    //log("webService prize: ${result.data}")
                }
            }
            suspendOnError { result = Resource.error(R.string.error_on_server, null) }
            suspendOnFailure { result = Resource.error(R.string.connection_error, null) }
        }

        return result
    }

    suspend fun getRiddle(getRiddle: GetRiddle): Resource<String> {
        var result: Resource<String> = Resource.error(R.string.unknown_error, null)

        val response = serverApi.getRiddle(getRiddle)
        log("webService: ${getRiddle.next}")
        with(response) {
            suspendOnSuccess {
                if (this.data != null) {
                    result = Resource.success(this.data!!.riddle)
                    log("webService getting riddle: ${result.data}")
                }
            }
            suspendOnError { result = Resource.error(R.string.error_on_server, null) }
            suspendOnFailure { result = Resource.error(R.string.connection_error, null) }
        }

        return result
    }

    suspend fun checkAnswer(checkAnswer: CheckAnswer): Resource<String> {
        var result: Resource<String> = Resource.error(R.string.unknown_error, null)

        val serverResponse = serverApi.checkAnswer(checkAnswer)
        with(serverResponse) {
            suspendOnSuccess {
                if(this.data != null) {
                    result = Resource.success(this.data!!.string())
                    log("check answer result: " + result.data!!)
                }
            }
            suspendOnError { result = Resource.error(R.string.error_on_server, null) }
            suspendOnFailure { result = Resource.error(R.string.connection_error, null) }
        }

        return result
    }

    suspend fun checkAppVersion(version: Int): Resource<UpdateType> {
        var result: Resource<UpdateType> = Resource.error(R.string.unknown_error, null)

        val serverResponse = serverApi.checkUpdate(version)
        with(serverResponse) {
            suspendOnSuccess {
                if(this.data != null) {
                    result = when(this.data!!.resultCode) {
                        5 -> Resource.success(UpdateType.SOFT_UPDATE)
                        6 -> Resource.success(UpdateType.FORCE_UPDATE)
                        else -> Resource.success(UpdateType.CURRENT)
                    }
                }
            }
            suspendOnError { result = Resource.error(R.string.error_on_server, null) }
            suspendOnFailure { result = Resource.error(R.string.connection_error, null) }
        }

        return result
    }

    suspend fun getPlace(getPlace: GetPlace): Resource<Int> {
        var result: Resource<Int> = Resource.error(R.string.unknown_error, null)

        val serverResponse = serverApi.getPlace(getPlace)
        with(serverResponse) {
            suspendOnSuccess {
                if(this.data != null) {
                    result = Resource.success(this.data!!.string().toInt())
                    log("webService: place = ${result.data}")
                }
            }
            suspendOnError { result = Resource.error(R.string.error_on_server, null) }
            suspendOnFailure { result = Resource.error(R.string.connection_error, null) }
        }

        return result
    }

    suspend fun getEmailContact(getEmail: GetEmail): Resource<String> {
        var result: Resource<String> = Resource.error(R.string.unknown_error, null)

        val serverResponse = serverApi.getEmail(getEmail)
        with(serverResponse) {
            suspendOnSuccess {
                if(this.data != null) {
                    result = Resource.success(this.data!!.string())
                    log("webService: email = ${result.data}")
                }
            }
            suspendOnError { result = Resource.error(R.string.error_on_server, null) }
            suspendOnFailure { result = Resource.error(R.string.connection_error, null) }
        }

        return result
    }
}