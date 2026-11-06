package ar.edu.uade.piotask.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ar.edu.uade.piotask.data.local.dao.ActividadDao
import ar.edu.uade.piotask.data.local.dao.CategoriaDao
import ar.edu.uade.piotask.data.local.dao.TareaDao
import ar.edu.uade.piotask.data.local.entity.ActividadEntity
import ar.edu.uade.piotask.data.local.entity.CategoriaEntity
import ar.edu.uade.piotask.data.local.entity.TareaEntity

@Database(
    entities = [CategoriaEntity::class, TareaEntity::class, ActividadEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tareaDao(): TareaDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun actividadDao(): ActividadDao

    companion object {
        const val NOMBRE = "pio.db"
    }
}
