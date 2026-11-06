package ar.edu.uade.piotask.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

/**
 * El estado de atención no se guarda: se calcula con estos campos y la fecha actual.
 * La FK a categoría es RESTRICT: una categoría con tareas no se puede borrar sin reasignarlas.
 */
@Entity(
    tableName = "tareas",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoriaId"), Index("completadaEn")],
)
data class TareaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val categoriaId: Long,
    val progreso: Int,
    val deadline: LocalDate?,
    val pospuestaHasta: LocalDate?,
    val ventanaDeadlineDias: Int?,
    val ventanaInactividadDias: Int?,
    val creadaEn: Instant,
    val ultimaActividad: Instant,
    val completadaEn: Instant?,
)
