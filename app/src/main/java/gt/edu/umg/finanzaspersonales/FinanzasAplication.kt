package gt.edu.umg.finanzaspersonales

import android.app.Application
import gt.edu.umg.finanzaspersonales.datos.MovimientoRepositoryRoom

class FinanzasApplication : Application() {

    val repositorio by lazy {
        MovimientoRepositoryRoom(this)
    }

    val controlFinanzas by lazy {
        ControlFinanzas(repositorio)
    }
}
