package martian.riddles.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import martian.riddles.domain.AttemptsController
import martian.riddles.domain.PurchaseController
import martian.riddles.domain.StatisticsController
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ControllersModule {

    @Provides
    @Singleton
    fun getAttemptsController(
        statisticsController: StatisticsController,
        sharedPreferences: SharedPreferences,
        editor: SharedPreferences.Editor
    ): AttemptsController {
        return AttemptsController(statisticsController, sharedPreferences, editor)
    }

    @Provides
    @Singleton
    fun getStatisticsController(@ApplicationContext context: Context): StatisticsController {
        return StatisticsController(context)
    }

    @Provides
    @Singleton
    fun getPurchaseController(
        @ApplicationContext context: Context,
        statisticsController: StatisticsController,
        attemptsController: AttemptsController
    ): PurchaseController {
        return PurchaseController(context, statisticsController, attemptsController)
    }
}