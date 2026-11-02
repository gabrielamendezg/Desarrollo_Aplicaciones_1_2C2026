package ar.edu.uade.piotask.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object TiempoModule {

    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
