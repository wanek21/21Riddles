package martian.riddles.data.remote

import com.skydoves.sandwich.ApiResponse
import martian.riddles.dto.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ServerApi {

    // проверка обновлений приложения
    @GET("/updates/")
    suspend fun checkUpdate(@Query("version") versionCode: Int): Call<ResponseFromServer?>?

    // получение приза
    @Headers("User-Agent: dont touch")
    @GET("/main/prize/")
    suspend fun getPrize(@Query("prize") queryTrue: String?, @Query("locale") locale: String?): ApiResponse<Prize>

    // регистрация логина
    @Headers("User-Agent: dont touch")
    @POST("/users/add/")
    suspend fun singUp(@Body registerUser: RegisterUser?): ApiResponse<StandardResponse>

    // получение списка лидеров
    @GET("/users/leaders/")
    suspend fun getLeaders(@Query("lead") lead: String?): ApiResponse<List<Leaders>>

    // узнать место игрока по нику
    @POST("/users/place/")
    suspend fun getPlace(@Body getPlace: GetPlace?): ApiResponse<ResponseBody>

    // получение загадки
    @Headers("User-Agent: dont touch")
    @POST("/riddles/")
    suspend fun getRiddle(@Body getRiddle: GetRiddle?): ApiResponse<Riddle>

    // получение email
    @Headers("User-Agent: dont touch")
    @POST("/main/email/")
    suspend fun getEmail(@Body getEmail: GetEmail?): ApiResponse<ResponseBody>

    // проверка ответа
    @POST("/riddles/answer/")
    suspend fun checkAnswer(@Body checkAnswer: CheckAnswer?): ApiResponse<ResponseBody>
}