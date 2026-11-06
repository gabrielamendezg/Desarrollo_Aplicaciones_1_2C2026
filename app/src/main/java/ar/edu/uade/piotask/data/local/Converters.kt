package ar.edu.uade.piotask.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate

/** Instant como milisegundos y LocalDate como día de época, para poder comparar en SQL. */
class Converters {
    @TypeConverter
    fun instantAEpoch(valor: Instant?): Long? = valor?.toEpochMilli()

    @TypeConverter
    fun epochAInstant(valor: Long?): Instant? = valor?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun fechaAEpochDay(valor: LocalDate?): Long? = valor?.toEpochDay()

    @TypeConverter
    fun epochDayAFecha(valor: Long?): LocalDate? = valor?.let(LocalDate::ofEpochDay)
}
