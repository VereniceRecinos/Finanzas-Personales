package gt.edu.umg.finanzaspersonales.datos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MovimientoDao {

    @Insert
    suspend fun insertar(movimiento: MovimientoEntity): Long

    @Query("SELECT * FROM movimientos ORDER BY id")
    suspend fun listar(): List<MovimientoEntity>

    @Query("SELECT * FROM movimientos WHERE id = :id")
    suspend fun buscarPorId(id: Int): MovimientoEntity?

    @Update
    suspend fun actualizar(movimiento: MovimientoEntity): Int

    @Query("DELETE FROM movimientos WHERE id = :id")
    suspend fun eliminar(id: Int): Int
}
