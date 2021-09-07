package martian.riddles.data.repositories

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import martian.riddles.BuildConfig
import martian.riddles.data.local.DataKeys
import martian.riddles.data.local.GameInfoDao
import martian.riddles.data.remote.WebService
import martian.riddles.util.Resource
import martian.riddles.util.Status
import martian.riddles.util.UpdateType
import martian.riddles.util.log
import javax.inject.Inject

class GameInfoRepository @Inject constructor(
    private val webService: WebService,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
) {

    suspend fun getPrize(locale: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            val prize = sharedPreferences.getString(DataKeys.PRIZE.key, "100")
            refreshPrize(locale)
            Resource.success(prize)
        }
    }

    private suspend fun refreshPrize(locale: String) {
        val remoteData = webService.getPrize(locale)
        if (remoteData.status == Status.SUCCESS) {
            if(remoteData.data != null) {
                editor.putString(DataKeys.PRIZE.key, remoteData.data)
                editor.commit()
            }
        }
    }

    suspend fun checkAppVersion(): Resource<UpdateType> {
        return withContext(Dispatchers.IO) {
            webService.checkAppVersion(BuildConfig.VERSION_CODE)
        }
    }

    fun getCountLaunchApp(): Int {
        return sharedPreferences.getInt(DataKeys.COUNT_LAUNCH_APP.key, 0)
    }

    // увеличить кол-во запусков приложения на 1
    fun upCountLaunchApp() {
        val currentCount = sharedPreferences.getInt(DataKeys.COUNT_LAUNCH_APP.key, 0)
        editor.putInt(DataKeys.COUNT_LAUNCH_APP.key,(currentCount+1))
        editor.commit()
    }
}