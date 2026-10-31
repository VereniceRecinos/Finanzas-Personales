package gt.edu.umg.finanzaspersonales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import gt.edu.umg.finanzaspersonales.ui.theme.FinanzasPersonalesTheme
import java.text.NumberFormat
import gt.edu.umg.finanzaspersonales.Movimiento
import java.time.LocalDate
import java.util.Locale
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.currentBackStackEntryAsState


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FinanzasPersonalesTheme {

                val navController = rememberNavController()

                val aplicacion = applicationContext as FinanzasApplication

                val repositorio = aplicacion.repositorio

                val controlFinanzas = aplicacion.controlFinanzas

                var saldoDisponible by remember {
                    mutableStateOf(
                        controlFinanzas.calcularSaldoDisponible()
                    )
                }

                // Permite actualizar la lista del balance.
                var versionBalance by remember {
                    mutableStateOf(0)
                }

                val totalIngresos = remember(controlFinanzas, versionBalance) {
                    controlFinanzas.calcularTotalIngresos()
                }

                val totalGastos = remember(controlFinanzas, versionBalance) {
                    controlFinanzas.calcularTotalEgresos()
                }


                val entradaActual by navController.currentBackStackEntryAsState()
                val rutaActual = entradaActual?.destination?.route

                Scaffold(
                    bottomBar = {
                        NavigationBar {

                            NavigationBarItem(
                                selected = rutaActual == "inicio",
                                onClick = {
                                    navController.navigate("inicio") {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Inicio"
                                    )
                                },
                                label = { Text("Inicio") }
                            )

                            NavigationBarItem(
                                selected = rutaActual == "balance",
                                onClick = {
                                    navController.navigate("balance") {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = "Movimientos"
                                    )
                                },
                                label = { Text("Movimientos") }
                            )

                            NavigationBarItem(
                                selected = rutaActual == "resumen",
                                onClick = {
                                    navController.navigate("resumen") {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = "Resumen"
                                    )
                                },
                                label = { Text("Resumen") }
                            )
                        }
                    }
                ) { espacioInferior ->

                    NavHost(
                        navController = navController,
                        startDestination = "inicio",
                        modifier = Modifier.padding(espacioInferior)
                    ) {

                        composable("inicio") {
                            PantallaPrincipal(
                                saldoDisponible = saldoDisponible,
                                totalIngresos = totalIngresos,
                                totalGastos = totalGastos,
                                onAgregar = {
                                    navController.navigate("agregar")
                                }

                            )
                        }

                        composable("agregar") {
                            PantallaMovimientos(
                                controlFinanzas = controlFinanzas,
                                onMovimientoGuardado = {
                                    saldoDisponible =
                                        controlFinanzas.calcularSaldoDisponible()

                                    versionBalance++
                                }
                            )
                        }

                        composable("balance") {
                            PantallaBalance(
                                controlFinanzas = controlFinanzas,
                                versionBalance = versionBalance,
                                onMovimientoModificado = {
                                    saldoDisponible =
                                        controlFinanzas.calcularSaldoDisponible()

                                    versionBalance++
                                },
                                onVolver = {
                                    navController.navigate("inicio") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable("resumen") {
                            PantallaResumen(
                                controlFinanzas = controlFinanzas,
                                onVolver = {
                                    navController.navigate("inicio") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }

            }
        }
    }
}


// --------------------------------------------------
// PANTALLA PRINCIPAL
// --------------------------------------------------


@Composable
fun PantallaPrincipal(
    saldoDisponible: Double,
    totalIngresos: Double,
    totalGastos: Double,
    onAgregar: () -> Unit
) {
    val formatoMoneda = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 40.dp,
                bottom = 20.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "¡BIENVENIDO A TUS FINANZAS!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Saldo disponible
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Saldo disponible",
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Q ${formatoMoneda.format(saldoDisponible)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Balance general
        Text(
            text = "Balance general",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Total de ingresos
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Total de ingresos",
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Q ${formatoMoneda.format(totalIngresos)}",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Total de gastos
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Total de gastos",
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Q ${formatoMoneda.format(totalGastos)}",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón para agregar ingresos o gastos
        Button(
            onClick = onAgregar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar Ingreso o Gasto")
        }

    }
}




@Composable
fun PantallaResumen(
    controlFinanzas: ControlFinanzas,
    onVolver: () -> Unit
) {

    val fechaActual = java.time.YearMonth.now()

    var periodoActual by remember {
        mutableStateOf(fechaActual)
    }

    var menuMesAbierto by remember {
        mutableStateOf(false)
    }

    var menuAnioAbierto by remember {
        mutableStateOf(false)
    }

    val primerAnioDisponible = remember(controlFinanzas) {
        controlFinanzas.listarMovimientos()
            .minOfOrNull { it.fecha.year } ?: fechaActual.year
    }

    val resumen = remember(controlFinanzas, periodoActual) {
        controlFinanzas.generarResumenMensual(periodoActual)
    }

    val formatoMoneda = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    val nombreMes = remember(periodoActual) {
        periodoActual.month.getDisplayName(
            java.time.format.TextStyle.FULL,
            Locale("es", "GT")
        ).replaceFirstChar { it.uppercase() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 40.dp,
                bottom = 20.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Resumen Mensual",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Selectores de mes y año
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Selector de mes
            Box(modifier = Modifier.weight(1f)) {

                OutlinedButton(
                    onClick = { menuMesAbierto = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(nombreMes)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Seleccionar mes"
                    )
                }

                DropdownMenu(
                    expanded = menuMesAbierto,
                    onDismissRequest = { menuMesAbierto = false }
                ) {
                    java.time.Month.values()
                        .forEach { mes ->

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        mes.getDisplayName(
                                            java.time.format.TextStyle.FULL,
                                            Locale("es", "GT")
                                        ).replaceFirstChar { it.uppercase() }
                                    )
                                },
                                onClick = {
                                    periodoActual = periodoActual.withMonth(mes.value)
                                    menuMesAbierto = false
                                }
                            )
                        }
                }
            }

            // Selector de año
            Box(modifier = Modifier.weight(1f)) {

                OutlinedButton(
                    onClick = { menuAnioAbierto = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(periodoActual.year.toString())
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Seleccionar año"
                    )
                }

                DropdownMenu(
                    expanded = menuAnioAbierto,
                    onDismissRequest = { menuAnioAbierto = false }
                ) {
                    (primerAnioDisponible..fechaActual.year)
                        .reversed()
                        .forEach { anio ->

                            DropdownMenuItem(
                                text = { Text(anio.toString()) },
                                onClick = {
                                    val nuevoPeriodo = periodoActual.withYear(anio)

                                    periodoActual = if (
                                        nuevoPeriodo.isAfter(fechaActual)
                                    ) {
                                        fechaActual
                                    } else {
                                        nuevoPeriodo
                                    }

                                    menuAnioAbierto = false
                                }
                            )
                        }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Total de ingresos
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp)) {
                Text(
                    text = "Total de ingresos",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Q ${formatoMoneda.format(resumen.totalIngresos)}",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Total de egresos
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Total de gastos",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Q ${formatoMoneda.format(resumen.totalEgresos)}",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Resultado mensual
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = when {
                        resumen.huboSuperavit -> "Superávit del mes"
                        resumen.huboDeficit -> "Déficit del mes"
                        else -> "Ingresos y gastos iguales"
                    },
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Q ${formatoMoneda.format(resumen.resultado)}",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Porcentaje de egresos respecto a ingresos
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Porcentaje de gastos respecto a ingresos",
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${formatoMoneda.format(resumen.porcentajeEgresos)} %",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}



// --------------------------------------------------
// PANTALLA DE INGRESOS Y EGRESOS
// --------------------------------------------------

@Composable
fun PantallaMovimientos(
    controlFinanzas: ControlFinanzas,
    onMovimientoGuardado: () -> Unit
) {
    var descripcionIngreso by remember { mutableStateOf("") }
    var montoIngreso by remember { mutableStateOf("") }
    var categoriaIngreso by remember { mutableStateOf("") }
    var mensajeIngreso by remember { mutableStateOf("") }

    var descripcionEgreso by remember { mutableStateOf("") }
    var montoEgreso by remember { mutableStateOf("") }
    var categoriaEgreso by remember { mutableStateOf("") }
    var mensajeEgreso by remember { mutableStateOf("") }

    val categoriasIngreso = listOf(
        "Salario",
        "Apoyo familiar",
        "Negocio o emprendimiento",
        "Trabajo independiente",
        "Beca o ayuda educativa",
        "Alquileres",
        "Otros ingresos"
    )

    val categoriasEgreso = listOf(
        "Alimentación",
        "Supermercado y despensa",
        "Transporte y gasolina",
        "Universidad y estudios",
        "Comidas fuera de casa",
        "Vivienda y servicios",
        "Salud y cuidado personal",
        "Entretenimiento",
        "Otros gastos"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 20.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Agregar Ingreso o Gasto",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Registrar ingreso",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = descripcionIngreso,
            onValueChange = {
                descripcionIngreso = it
                mensajeIngreso = ""
            },
            label = { Text("Descripción del ingreso") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = montoIngreso,
            onValueChange = {
                montoIngreso = it
                mensajeIngreso = ""
            },
            label = { Text("Monto (Q)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        SelectorCategoria(
            titulo = "Categoría del ingreso",
            categorias = categoriasIngreso,
            categoriaSeleccionada = categoriaIngreso,
            onCategoriaSeleccionada = {
                categoriaIngreso = it
                mensajeIngreso = ""
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val monto = montoIngreso.trim().toDoubleOrNull()

                when {
                    descripcionIngreso.isBlank() -> {
                        mensajeIngreso =
                            "Escribe la descripción del ingreso."
                    }

                    monto == null || !monto.isFinite() || monto <= 0 -> {
                        mensajeIngreso =
                            "Ingresa un monto válido mayor que cero."
                    }

                    categoriaIngreso.isBlank() -> {
                        mensajeIngreso =
                            "Selecciona una categoría para el ingreso."
                    }

                    else -> {
                        try {
                            val categoria = Categoria(
                                id = categoriasIngreso.indexOf(
                                    categoriaIngreso
                                ) + 1,
                                nombre = categoriaIngreso,
                                tipoMovimiento = TipoMovimiento.INGRESO
                            )

                            controlFinanzas.registrarMovimiento(
                                descripcion = descripcionIngreso.trim(),
                                monto = monto,
                                tipoMovimiento = TipoMovimiento.INGRESO,
                                categoria = categoria,
                                fecha = LocalDate.now()
                            )

                            descripcionIngreso = ""
                            montoIngreso = ""
                            categoriaIngreso = ""

                            onMovimientoGuardado()

                            mensajeIngreso =
                                "Ingreso guardado correctamente."

                        } catch (e: IllegalArgumentException) {
                            mensajeIngreso = e.message
                                ?: "No se pudo guardar el ingreso."
                        } catch (e: Exception) {
                            mensajeIngreso =
                                "Error al guardar: ${e.message ?: e.javaClass.simpleName}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar ingreso")
        }

        if (mensajeIngreso.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mensajeIngreso,
                color = if (
                    mensajeIngreso == "Ingreso guardado correctamente."
                ) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Registrar gastos",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = descripcionEgreso,
            onValueChange = {
                descripcionEgreso = it
                mensajeEgreso = ""
            },
            label = { Text("Descripción del gasto") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = montoEgreso,
            onValueChange = {
                montoEgreso = it
                mensajeEgreso = ""
            },
            label = { Text("Monto (Q)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        SelectorCategoria(
            titulo = "Categoría del gasto",
            categorias = categoriasEgreso,
            categoriaSeleccionada = categoriaEgreso,
            onCategoriaSeleccionada = {
                categoriaEgreso = it
                mensajeEgreso = ""
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val monto = montoEgreso.trim().toDoubleOrNull()

                when {
                    descripcionEgreso.isBlank() -> {
                        mensajeEgreso =
                            "Escribe la descripción del gasto."
                    }

                    monto == null || !monto.isFinite() || monto <= 0 -> {
                        mensajeEgreso =
                            "Ingresa un monto válido mayor que cero."
                    }

                    categoriaEgreso.isBlank() -> {
                        mensajeEgreso =
                            "Selecciona una categoría para el gasto."
                    }

                    else -> {
                        try {
                            val categoria = Categoria(
                                id = categoriasEgreso.indexOf(
                                    categoriaEgreso
                                ) + 1,
                                nombre = categoriaEgreso,
                                tipoMovimiento = TipoMovimiento.EGRESO
                            )

                            controlFinanzas.registrarMovimiento(
                                descripcion = descripcionEgreso.trim(),
                                monto = monto,
                                tipoMovimiento = TipoMovimiento.EGRESO,
                                categoria = categoria,
                                fecha = LocalDate.now()
                            )

                            descripcionEgreso = ""
                            montoEgreso = ""
                            categoriaEgreso = ""

                            onMovimientoGuardado()

                            mensajeEgreso =
                                "Gasto guardado correctamente."

                        } catch (e: IllegalArgumentException) {
                            mensajeEgreso = e.message
                                ?: "No se pudo guardar el gasto."
                        } catch (e: Exception) {
                            mensajeEgreso =
                                "Error al guardar: ${e.message ?: e.javaClass.simpleName}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar gasto")
        }

        if (mensajeEgreso.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mensajeEgreso,
                color = if (
                    mensajeEgreso == "Gasto guardado correctamente."
                ) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// --------------------------------------------------
// PANTALLA DE BALANCE GENERAL
// --------------------------------------------------


@Composable
fun PantallaBalance(
    controlFinanzas: ControlFinanzas,
    versionBalance: Int,
    onMovimientoModificado: () -> Unit,
    onVolver: () -> Unit
) {
    var movimientoEnEdicion by remember { mutableStateOf<Movimiento?>(null) }
    var movimientoParaEliminar by remember { mutableStateOf<Movimiento?>(null) }
    var mensajeAccion by remember { mutableStateOf("") }
    var filtroSeleccionado by remember {
        mutableStateOf("Todos")
    }

    // La lista se vuelve a consultar cuando se guarda, edita o elimina un movimiento.
    val movimientos = remember(controlFinanzas, versionBalance) {
        controlFinanzas.listarMovimientos()
    }

    val movimientosFiltrados = remember(movimientos, filtroSeleccionado) {
        when (filtroSeleccionado) {
            "Ingresos" -> movimientos.filter {
                it.tipoMovimiento == TipoMovimiento.INGRESO
            }

            "Gastos" -> movimientos.filter {
                it.tipoMovimiento == TipoMovimiento.EGRESO
            }

            else -> movimientos
        }
    }

    val formatoMoneda = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 40.dp,
                bottom = 20.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Movimientos registrados",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Todos", "Ingresos", "Gastos").forEach { filtro ->

                Button(
                    onClick = {
                        filtroSeleccionado = filtro
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (filtroSeleccionado == filtro) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (filtroSeleccionado == filtro) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                ) {
                    Text(
                        text = filtro,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (movimientosFiltrados.isEmpty()) {
            Text(
                text = if (movimientos.isEmpty()) {
                    "Todavía no has registrado movimientos."
                } else {
                    "No hay movimientos en esta categoría."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            movimientosFiltrados.forEach { movimiento ->

                val esIngreso =
                    movimiento.tipoMovimiento == TipoMovimiento.INGRESO

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = movimiento.descripcion,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = if (esIngreso) {
                                    "+ Q ${formatoMoneda.format(movimiento.monto)}"
                                } else {
                                    "- Q ${formatoMoneda.format(movimiento.monto)}"
                                },
                                fontWeight = FontWeight.Bold,
                                color = if (esIngreso) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (esIngreso) "Ingreso" else "Gasto",
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Categoría: ${movimiento.categoria.nombre}"
                        )

                        Text(
                            text = "Fecha: ${movimiento.fecha}"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    movimientoEnEdicion = movimiento
                                    mensajeAccion = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Editar")
                            }

                            Button(
                                onClick = { movimientoParaEliminar = movimiento },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
            }
        }

        if (mensajeAccion.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mensajeAccion,
                color = if (mensajeAccion.startsWith("Movimiento actualizado") ||
                    mensajeAccion.startsWith("Movimiento eliminado")) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }

    // Diálogo para editar un movimiento.
    movimientoEnEdicion?.let { movimiento ->
        DialogoEditarMovimiento(
            movimiento = movimiento,
            onCancelar = {
                movimientoEnEdicion = null
            },
            onGuardar = { descripcion, monto, categoriaNombre ->
                try {
                    val categorias = if (
                        movimiento.tipoMovimiento == TipoMovimiento.INGRESO
                    ) {
                        listOf(
                            "Salario", "Apoyo familiar", "Negocio o emprendimiento",
                            "Trabajo independiente", "Beca o ayuda educativa",
                            "Alquileres", "Otros ingresos"
                        )
                    } else {
                        listOf(
                            "Alimentación", "Supermercado y despensa",
                            "Transporte y gasolina", "Universidad y estudios",
                            "Comidas fuera de casa", "Vivienda y servicios",
                            "Salud y cuidado personal", "Entretenimiento", "Otros gastos"
                        )
                    }

                    val categoriaActualizada = Categoria(
                        id = categorias.indexOf(categoriaNombre) + 1,
                        nombre = categoriaNombre,
                        tipoMovimiento = movimiento.tipoMovimiento
                    )

                    val movimientoActualizado = movimiento.copy(
                        descripcion = descripcion.trim(),
                        monto = monto,
                        categoria = categoriaActualizada
                    )

                    val actualizado = controlFinanzas.actualizarMovimiento(
                        movimientoActualizado
                    )

                    if (actualizado) {
                        movimientoEnEdicion = null
                        mensajeAccion = "Movimiento actualizado correctamente."
                        onMovimientoModificado()
                    } else {
                        mensajeAccion = "No se pudo actualizar el movimiento."
                    }
                } catch (e: Exception) {
                    mensajeAccion = e.message ?: "Ocurrió un error al editar."
                }
            }
        )
    }

    // Confirmación antes de eliminar.
    movimientoParaEliminar?.let { movimiento ->
        AlertDialog(
            onDismissRequest = {
                movimientoParaEliminar = null
            },
            title = {
                Text("Eliminar movimiento")
            },
            text = {
                Text("¿Seguro que deseas eliminar \"${movimiento.descripcion}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val eliminado = controlFinanzas.eliminarMovimiento(movimiento.id)
                        movimientoParaEliminar = null

                        if (eliminado) {
                            mensajeAccion = "Movimiento eliminado correctamente."
                            onMovimientoModificado()
                        } else {
                            mensajeAccion = "No se pudo eliminar el movimiento."
                        }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        movimientoParaEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}


@Composable
fun DialogoEditarMovimiento(
    movimiento: Movimiento,
    onCancelar: () -> Unit,
    onGuardar: (String, Double, String) -> Unit
) {
    var descripcion by remember(movimiento.id) {
        mutableStateOf(movimiento.descripcion)
    }
    var montoTexto by remember(movimiento.id) {
        mutableStateOf(movimiento.monto.toString())
    }
    var categoria by remember(movimiento.id) {
        mutableStateOf(movimiento.categoria.nombre)
    }
    var error by remember(movimiento.id) {
        mutableStateOf("")
    }

    val categorias = if (movimiento.tipoMovimiento == TipoMovimiento.INGRESO) {
        listOf(
            "Salario", "Apoyo familiar", "Negocio o emprendimiento",
            "Trabajo independiente", "Beca o ayuda educativa",
            "Alquileres", "Otros ingresos"
        )
    } else {
        listOf(
            "Alimentación", "Supermercado y despensa",
            "Transporte y gasolina", "Universidad y estudios",
            "Comidas fuera de casa", "Vivienda y servicios",
            "Salud y cuidado personal", "Entretenimiento", "Otros gastos"
        )
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Editar movimiento") },
        text = {
            Column {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it; error = "" },
                    label = { Text("Descripción") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { montoTexto = it; error = "" },
                    label = { Text("Monto (Q)") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                SelectorCategoria(
                    titulo = "Categoría",
                    categorias = categorias,
                    categoriaSeleccionada = categoria,
                    onCategoriaSeleccionada = { categoria = it; error = "" }
                )
                if (error.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val monto = montoTexto.trim().toDoubleOrNull()
                    when {
                        descripcion.isBlank() -> error = "Escribe una descripción."
                        monto == null || !monto.isFinite() || monto <= 0 ->
                            error = "Ingresa un monto válido mayor que cero."
                        categoria.isBlank() -> error = "Selecciona una categoría."
                        else -> onGuardar(descripcion, monto, categoria)
                    }
                }
            ) {
                Text("Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

// --------------------------------------------------
// SELECTOR DE CATEGORÍAS
// --------------------------------------------------

@Composable
fun SelectorCategoria(
    titulo: String,
    categorias: List<String>,
    categoriaSeleccionada: String,
    onCategoriaSeleccionada: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = titulo,
            fontWeight = FontWeight.Medium
        )

        OutlinedButton(
            onClick = {
                expandido = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (categoriaSeleccionada.isBlank()) {
                    "Seleccionar categoría"
                } else {
                    categoriaSeleccionada
                }
            )
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = {
                expandido = false
            }
        ) {
            categorias.forEach { categoria ->
                DropdownMenuItem(
                    text = {
                        Text(categoria)
                    },
                    onClick = {
                        onCategoriaSeleccionada(categoria)
                        expandido = false
                    }
                )
            }
        }
    }
}
