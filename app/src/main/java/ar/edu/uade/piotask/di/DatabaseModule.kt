package ar.edu.uade.piotask.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import ar.edu.uade.piotask.data.local.AppDatabase
import ar.edu.uade.piotask.data.local.dao.ActividadDao
import ar.edu.uade.piotask.data.local.dao.CategoriaDao
import ar.edu.uade.piotask.data.local.dao.TareaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NOMBRE).build()

    @Provides
    fun provideTareaDao(database: AppDatabase): TareaDao = database.tareaDao()

    @Provides
    fun provideCategoriaDao(database: AppDatabase): CategoriaDao = database.categoriaDao()

    @Provides
    fun provideActividadDao(database: AppDatabase): ActividadDao = database.actividadDao()

    @Provides
    @Singleton
    fun providePreferencias(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("preferencias") }
}
