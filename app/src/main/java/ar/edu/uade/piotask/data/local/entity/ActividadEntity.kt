package ar.edu.uade.piotask.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ar.edu.uade.piotask.domain.model.TipoActividad
import java.time.Instant

@Entity(
    tableName = "actividades",
    foreignKeys = [
        ForeignKey(
            entity = TareaEntity::class,
            parentColumns = ["id"],
            childColumns = ["tareaId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("tareaId")],
)
data class ActividadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tareaId: Long,
    val fecha: Instant,
    val tipo: TipoActividad,
    val progreso: Int?,
)
