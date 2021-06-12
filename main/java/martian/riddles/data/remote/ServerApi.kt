package martian.riddles.data.remote

import com.skydoves.sandwich.ApiResponse
import martian.riddles.dto.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ServerApi {

    // проверка обновлений приложения
    @GET("/updates/")
    fun checkUpdate(@Query("version") versionCode: Int): Call<ResponseFromServer?>?

    // получение приза
    @Headers("User-Agent: dont touch")
    @GET("/main/prize/")
    fun getPrize(@Query("prize") queryTrue: String?, @Query("locale") locale: String?): Call<Prize?>?

    // регистрация логина
    @Headers("User-Agent: dont touch")
    @POST("/users/add/")
    suspend fun singUp(@Body registerUser: RegisterUser?): ApiResponse<StandardResponse>

    // получение списка лидеров
    @GET("/users/leaders/")
    fun getLeaders(@Query("lead") lead: String?): Call<List<Leaders?>?>?

    // узнать место игрока по нику
    @POST("/users/place/")
    fun getPlace(@Body getPlace: GetPlace?): Call<ResponseBody?>?

    // получение загадки
    @Headers("User-Agent: dont touch")
    @POST("/riddles/")
    fun getRiddle(@Body getRiddle: GetRiddle?): Call<Riddle?>?

    // получение email
    @Headers("User-Agent: dont touch")
    @POST("/main/email/")
    fun getEmail(@Body getEmail: GetEmail?): Call<ResponseBody?>?

    // проверка ответа
    @POST("/riddles/answer/")
    fun checkAnswer(@Body checkAnswer: CheckAnswer?): Call<ResponseBody?>?
}