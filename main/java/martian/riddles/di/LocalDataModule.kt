package martian.riddles.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import martian.riddles.data.local.*
import martian.riddles.util.GetContextClass
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class LocalDataModule {

    private val APP_PREFERENCES = "app_data"

    @Singleton
    @Provides
    fun getSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
    }

    @Provides
    fun getEdit(sharedPreferences: SharedPreferences): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    @Singleton
    @Provides
    fun getDb(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
                context,
                AppDatabase::class.java, "riddle_game"
        ).build()
    }

    @Provides
    fun usersDao(appDatabase: AppDatabase): UsersDao {
        return appDatabase.usersDao()
    }

    @Provides
    fun riddlesDao(appDatabase: AppDatabase): RiddlesDao {
        return appDatabase.riddlesDao()
    }

    @Provides
    fun gameInfoDao(appDatabase: AppDatabase): GameInfoDao {
        return appDatabase.gameInfoDao()
    }
}

