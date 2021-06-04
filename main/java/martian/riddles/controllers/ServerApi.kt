package martian.riddles.controllers

import martian.riddles.dto.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ServerApi {
    @GET("/updates/")
    fun  // проверка обновлений приложения
            checkUpdate(@Query("version") versionCode: Int): Call<ResponseFromServer?>?

    @Headers("User-Agent: dont touch")
    @GET("/main/prize/")
    fun  // получение приза
            getPrize(@Query("prize") queryTrue: String?, @Query("locale") locale: String?): Call<Prize?>?

    @Headers("User-Agent: dont touch")
    @POST("/users/add/")
    fun  // регистрация логина
            logup(@Body dataOfUser: DataOfUser?): Call<ResponseFromServer?>?

    @GET("/users/leaders/")
    fun  // получение списка лидеров
            getLeaders(@Query("lead") lead: String?): Call<List<Leaders?>?>?

    @POST("/users/place/")
    fun  // узнать место игрока по нику
            getPlace(@Body getPlace: GetPlace?): Call<ResponseBody?>?

    @Headers("User-Agent: dont touch")
    @POST("/riddles/")
    fun  // получение загадки
            getRiddle(@Body getRiddle: GetRiddle?): Call<Riddle?>?

    @Headers("User-Agent: dont touch")
    @POST("/main/email/")
    fun  // получение email
            getEmail(@Body getEmail: GetEmail?): Call<ResponseBody?>?

    @POST("/riddles/answer/")
    fun  // проверка ответа
            checkAnswer(@Body checkAnswer: CheckAnswer?): Call<ResponseBody?>?
}