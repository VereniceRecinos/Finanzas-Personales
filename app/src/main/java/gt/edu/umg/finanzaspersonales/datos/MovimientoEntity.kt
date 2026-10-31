package gt.edu.umg.finanzaspersonales.datos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movimientos")
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val descripcion: String,
    val monto: Double,
    val tipoMovimiento: String,
    val categoria: String,
    val fecha: String
)
