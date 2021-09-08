package martian.riddles.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import martian.riddles.data.repositories.RiddlesRepository
import martian.riddles.data.repositories.UsersRepository
import martian.riddles.domain.AttemptsController
import martian.riddles.domain.RiddlesController
import martian.riddles.domain.StatisticsController
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ControllersModule {

    @Provides
    @Singleton
    fun getAttemptsController(
        usersRepository: UsersRepository,
        statisticsController: StatisticsController
    ): AttemptsController {
        return AttemptsController(usersRepository, statisticsController)
    }

    @Provides
    @Singleton
    fun getRiddlesController(
        usersRepository: UsersRepository,
        riddlesRepository: RiddlesRepository,
        attemptsController: AttemptsController
    ): RiddlesController {
        return RiddlesController(usersRepository, riddlesRepository, attemptsController)
    }

    @Provides
    @Singleton
    fun getStatisticsController(@ApplicationContext context: Context, usersRepository: UsersRepository): StatisticsController {
        return StatisticsController(context, usersRepository)
    }
}