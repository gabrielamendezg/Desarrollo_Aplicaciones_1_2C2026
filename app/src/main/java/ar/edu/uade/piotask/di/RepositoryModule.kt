package ar.edu.uade.piotask.di

import ar.edu.uade.piotask.data.repository.ActividadRepositoryImpl
import ar.edu.uade.piotask.data.repository.CategoriaRepositoryImpl
import ar.edu.uade.piotask.data.repository.PreferenciasRepositoryImpl
import ar.edu.uade.piotask.data.repository.TareaRepositoryImpl
import ar.edu.uade.piotask.domain.repository.ActividadRepository
import ar.edu.uade.piotask.domain.repository.CategoriaRepository
import ar.edu.uade.piotask.domain.repository.PreferenciasRepository
import ar.edu.uade.piotask.domain.repository.TareaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindTareaRepository(impl: TareaRepositoryImpl): TareaRepository

    @Binds @Singleton
    abstract fun bindCategoriaRepository(impl: CategoriaRepositoryImpl): CategoriaRepository

    @Binds @Singleton
    abstract fun bindActividadRepository(impl: ActividadRepositoryImpl): ActividadRepository

    @Binds @Singleton
    abstract fun bindPreferenciasRepository(impl: PreferenciasRepositoryImpl): PreferenciasRepository
}
