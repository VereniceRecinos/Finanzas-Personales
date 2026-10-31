package gt.edu.umg.finanzaspersonales.datos

import android.content.Context
import androidx.room.Room
import gt.edu.umg.finanzaspersonales.Categoria
import gt.edu.umg.finanzaspersonales.Movimiento
import gt.edu.umg.finanzaspersonales.MovimientoRepository
import gt.edu.umg.finanzaspersonales.TipoMovimiento
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MovimientoRepositoryRoom(context: Context) : MovimientoRepository {

    private val baseDatos = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "finanzas_personales.db"
    ).build()

    private val dao = baseDatos.movimientoDao()

    override fun guardar(movimiento: Movimiento): Movimiento =
        runBlocking {
            withContext(Dispatchers.IO) {
                val entidad = movimiento.toEntity().copy(id = 0)
                val idGenerado = dao.insertar(entidad).toInt()

                movimiento.copy(id = idGenerado)
            }
        }

    override fun listarTodos(): List<Movimiento> =
        runBlocking {
            withContext(Dispatchers.IO) {
                dao.listar().map { it.toMovimiento() }
            }
        }

    override fun buscarPorId(id: Int): Movimiento? =
        runBlocking {
            withContext(Dispatchers.IO) {
                dao.buscarPorId(id)?.toMovimiento()
            }
        }

    override fun actualizar(movimiento: Movimiento): Boolean =
        runBlocking {
            withContext(Dispatchers.IO) {
                dao.actualizar(movimiento.toEntity()) > 0
            }
        }

    override fun eliminar(id: Int): Boolean =
        runBlocking {
            withContext(Dispatchers.IO) {
                dao.eliminar(id) > 0
            }
        }

    private fun Movimiento.toEntity(): MovimientoEntity =
        MovimientoEntity(
            id = id,
            descripcion = descripcion,
            monto = monto,
            tipoMovimiento = tipoMovimiento.name,
            categoria = categoria.nombre,
            fecha = fecha.toString()
        )

    private fun MovimientoEntity.toMovimiento(): Movimiento {
        val tipo = TipoMovimiento.valueOf(tipoMovimiento)

        val idCategoria = when (tipo) {
            TipoMovimiento.INGRESO -> listOf(
                "Salario",
                "Apoyo familiar",
                "Negocio o emprendimiento",
                "Trabajo independiente",
                "Beca o ayuda educativa",
                "Alquileres",
                "Otros ingresos"
            ).indexOf(categoria) + 1

            TipoMovimiento.EGRESO -> listOf(
                "Alimentación",
                "Supermercado y despensa",
                "Transporte y gasolina",
                "Universidad y estudios",
                "Comidas fuera de casa",
                "Vivienda y servicios",
                "Salud y cuidado personal",
                "Entretenimiento",
                "Otros egresos"
            ).indexOf(categoria) + 1
        }

        return Movimiento(
            id = id,
            descripcion = descripcion,
            monto = monto,
            tipoMovimiento = tipo,
            categoria = Categoria(
                id = idCategoria,
                nombre = categoria,
                tipoMovimiento = tipo
            ),
            fecha = LocalDate.parse(fecha)
        )
    }
}
