package gt.edu.umg.finanzaspersonales.datos

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MovimientoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movimientoDao(): MovimientoDao
}
