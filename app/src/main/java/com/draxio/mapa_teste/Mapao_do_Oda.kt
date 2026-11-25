package com.draxio.mapa_teste

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.draxio.mapa_teste.ui.theme.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay

class Mapao_do_Oda : FragmentActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isFirstZoom = true

    private val locationPermissionRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap?.let { map ->
                        if (isFirstZoom) {
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                            isFirstZoom = false
                        } else {
                            map.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                        }
                    }
                }
            }
        }

        setContent {
            Mapa_testeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent(
                        onMapReady = { googleMap ->
                            mMap = googleMap
                            enableMyLocation()
                        },
                        onToast = { message ->
                            Toast.makeText(this@Mapao_do_Oda, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap?.isMyLocationEnabled = true
            startLocationUpdates()
            return
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            locationPermissionRequestCode
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Logo "C caramelo"
@Composable
fun CarameloLogo(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(CarameloYellow, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "C",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Text(
            text = "caramelo",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            fontFamily = FontFamily.Serif
        )
    }
}

@Composable
fun MainAppContent(
    onMapReady: (GoogleMap) -> Unit = {},
    onToast: (String) -> Unit = {}
) {
    var isDrawerOpen by remember { mutableStateOf(false) }
    var showPresentation by remember { mutableStateOf(false) }
    var showTripHistory by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    when {
        showPresentation -> {
            CarameloPresentationScreen(
                onClose = { showPresentation = false }
            )
        }
        showTripHistory -> {
            CarameloTripHistoryScreen(
                onClose = { showTripHistory = false }
            )
        }
        showSettings -> {
            SettingsScreen(
                onClose = { showSettings = false },
                onToast = onToast
            )
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize()) {
                // Mapa base
                MapScreen(onMapReady = onMapReady)

                // Badge "12 motoristas por perto" no topo direito do mapa
                Surface(
                    modifier = Modifier
                        .padding(top = 48.dp, end = 16.dp)
                        .align(Alignment.TopEnd),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(CarameloGreen, CircleShape)
                        )
                        Text(
                            text = "12 motoristas por perto",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Painel Principal de solicitação de viagem (RideHailingOverlay)
                RideHailingOverlay(
                    onOpenDrawer = { isDrawerOpen = true },
                    onRideRequested = { dest, cat, driverPref ->
                        onToast("Buscando $cat ($driverPref) para $dest...")
                    }
                )

                // Drawer lateral / Menu do Usuário
                AnimatedVisibility(
                    visible = isDrawerOpen,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CarameloDrawerContent(
                        onClose = { isDrawerOpen = false },
                        onOpenTripHistory = {
                            isDrawerOpen = false
                            showTripHistory = true
                        },
                        onOpenSettings = {
                            isDrawerOpen = false
                            showSettings = true
                        },
                        onOpenPresentation = {
                            isDrawerOpen = false
                            showPresentation = true
                        },
                        onToast = onToast
                    )
                }
            }
        }
    }
}

@Composable
fun MapScreen(onMapReady: (GoogleMap) -> Unit) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🚖", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mapa do Caramelo",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val fragmentActivity = context as FragmentActivity
                androidx.fragment.app.FragmentContainerView(context).apply {
                    id = android.view.View.generateViewId()
                    tag = "map_fragment_container"
                    val mapFragment = SupportMapFragment.newInstance()
                    fragmentActivity.supportFragmentManager.beginTransaction()
                        .replace(this.id, mapFragment)
                        .commit()
                    mapFragment.getMapAsync { googleMap ->
                        onMapReady(googleMap)
                    }
                }
            },
            update = { _ -> }
        )
    }
}

enum class RideStatus {
    IDLE,
    SEARCHING_DRIVER,
    DRIVER_ACCEPTED
}

data class RideCategory(
    val name: String,
    val price: String,
    val timeAndType: String,
    val capacity: Int,
    val icon: String
)

data class DriverPreferenceOption(
    val id: String,
    val title: String,
    val icon: String
)

data class DriverOffer(
    val id: String,
    val driverName: String,
    val rating: String,
    val vehicle: String,
    val plate: String,
    val offeredPrice: String,
    val eta: String,
    val driverTypeTag: String,
    val icon: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHailingOverlay(
    onOpenDrawer: () -> Unit,
    onRideRequested: (String, String, String) -> Unit
) {
    var rideStatus by remember { mutableStateOf(RideStatus.IDLE) }
    var destination by remember { mutableStateOf("Shopping Cidade Jardim") }
    var origin by remember { mutableStateOf("Localização atual") }

    // Tipo de Motorista Escolhido
    val driverPreferences = remember {
        listOf(
            DriverPreferenceOption("Geral", "Geral", "👤"),
            DriverPreferenceOption("LGBTQIA+", "LGBTQIA+", "🏳️‍🌈"),
            DriverPreferenceOption("Mulher", "Mulher", "👩"),
            DriverPreferenceOption("Homem", "Homem", "👨")
        )
    }
    var selectedDriverPrefIndex by remember { mutableIntStateOf(0) }

    // Oferta de Preço com + e - e Slider (Deslizar)
    var offeredPriceAmount by remember { mutableFloatStateOf(18.40f) }

    // Stream de Propostas dos Motoristas
    val initialDriverOffers = remember {
        listOf(
            DriverOffer("1", "Carlos Silva", "4.9 ★", "Honda Civic", "ABC-1234", "R$ 18,40", "2 min", "🏳️‍🌈 LGBTQIA+", "🚖"),
            DriverOffer("2", "Fernanda Lima", "5.0 ★", "Toyota Corolla", "XYZ-5678", "R$ 19,00", "3 min", "👩 Mulher", "🚘"),
            DriverOffer("3", "Lucas Almeida", "4.8 ★", "Hyundai HB20", "KML-9012", "R$ 17,50", "1 min", "👤 Geral", "🚕")
        )
    }
    val currentOffers = remember { mutableStateListOf<DriverOffer>().apply { addAll(initialDriverOffers) } }
    var acceptedOffer by remember { mutableStateOf<DriverOffer?>(null) }

    val categories = remember {
        listOf(
            RideCategory("Carro", "R$ 18,40", "3 min • Carro Econômico", 4, "🚗"),
            RideCategory("Carro com Ar", "R$ 24,90", "4 min • Ar-Condicionado Gelando", 4, "❄️"),
            RideCategory("Moto", "R$ 12,00", "2 min • Viagem Rápida e Ágil", 1, "🏍️"),
            RideCategory("Entregas", "R$ 14,50", "3 min • Pacotes e Encomendas", 1, "📦"),
            RideCategory("Frete", "R$ 45,00", "8 min • Transporte de Cargas", 2, "🚚")
        )
    }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Overlay de busca (Encontrando seu motorista e Stream de Propostas) - Modal Dialog
        if (rideStatus == RideStatus.SEARCHING_DRIVER || rideStatus == RideStatus.DRIVER_ACCEPTED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Botão de fechar 'X'
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                            IconButton(
                                onClick = { rideStatus = RideStatus.IDLE },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text(text = "✕", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (rideStatus == RideStatus.SEARCHING_DRIVER) {
                            Text(
                                text = "PROPOSTAS DOS MOTORISTAS (${currentOffers.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Motoristas Aceitando Viagens",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Serif
                            )
                            Text(
                                text = "Toque na proposta para remover/recusar ou clique em Aceitar",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (currentOffers.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "Buscando novas propostas...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { currentOffers.addAll(initialDriverOffers) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Restaurar Propostas")
                                        }
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 280.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    currentOffers.toList().forEach { offer ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    // Removível ao toque direto no card
                                                    currentOffers.remove(offer)
                                                },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(text = offer.icon, fontSize = 22.sp)
                                                        Column {
                                                            Text(
                                                                text = offer.driverName,
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Text(
                                                                text = "${offer.vehicle} • ${offer.plate} • ${offer.rating}",
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }

                                                    Text(
                                                        text = offer.offeredPrice,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = MaterialTheme.colorScheme.surface
                                                    ) {
                                                        Text(
                                                            text = "${offer.driverTypeTag} • ${offer.eta}",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }

                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        OutlinedButton(
                                                            onClick = { currentOffers.remove(offer) },
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text("Recusar ✕", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                                        }

                                                        val contextLocal = androidx.compose.ui.platform.LocalContext.current
                                                        Button(
                                                            onClick = {
                                                                acceptedOffer = offer
                                                                rideStatus = RideStatus.DRIVER_ACCEPTED

                                                                // Salvar a nova corrida aceita no histórico local persistente
                                                                val newTrip = TripHistoryItem(
                                                                    date = "Hoje, agora",
                                                                    origin = origin,
                                                                    destination = destination,
                                                                    driverName = offer.driverName,
                                                                    driverRating = offer.rating,
                                                                    category = categories[selectedCategoryIndex].name,
                                                                    driverType = offer.driverTypeTag,
                                                                    price = offer.offeredPrice
                                                                )
                                                                val currentHistory = CarameloRepository.getTripHistory(contextLocal).toMutableList()
                                                                currentHistory.add(0, newTrip)
                                                                CarameloRepository.saveTripHistory(contextLocal, currentHistory)
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                                        ) {
                                                            Text("Aceitar", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            TextButton(
                                onClick = { rideStatus = RideStatus.IDLE },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Cancelar busca",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        } else {
                            val activeDriver = acceptedOffer ?: currentOffers.firstOrNull()
                            Text(
                                text = "MOTORISTA ACEITO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CarameloGreen,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = activeDriver?.driverName ?: "Carlos Silva",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${activeDriver?.vehicle ?: "Honda Civic"} • Placa: ${activeDriver?.plate ?: "ABC-1234"} • ${activeDriver?.rating ?: "4.9 ★"}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { rideStatus = RideStatus.IDLE },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = "Concluir Corrida",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Sheet / Card da Interface Principal
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.82f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Top Bar Interna do Painel: Menu, Logo, Pitch, MC Avatar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onOpenDrawer,
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "☰", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            }
                        }

                        CarameloLogo()

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = "📊", fontSize = 12.sp)
                                    Text(
                                        text = "Pitch",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CarameloRustDark, CircleShape)
                                    .clickable { onOpenDrawer() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "MC",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Seção "— SUA CIDADE, SEU CAMINHO"
                    Text(
                        text = "— SUA CIDADE, SEU CAMINHO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Para onde vamos?",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Card de Destino / Partida
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Partida
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "PARTIDA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = origin,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 18.dp),
                                color = MaterialTheme.colorScheme.outline
                            )

                            // Destino
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(CarameloYellow, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "DESTINO",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = destination,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "+", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SEÇÃO DE SELEÇÃO DE TIPO DE MOTORISTA (Geral, LGBTQIA+, Mulher, Homem)
                    Text(
                        text = "TIPO DE MOTORISTA PREFERIDO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        driverPreferences.forEachIndexed { idx, opt ->
                            val isSelected = (idx == selectedDriverPrefIndex)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedDriverPrefIndex = idx },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = opt.icon, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = opt.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Opção de Preferência de Viagem Fixa
                    var isFixedDriverPreference by remember { mutableStateOf(false) }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isFixedDriverPreference = !isFixedDriverPreference }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "📌", fontSize = 13.sp)
                            Text(
                                text = "Fixar esta preferência para todas as viagens",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Switch(
                            checked = isFixedDriverPreference,
                            onCheckedChange = { isFixedDriverPreference = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // CARD DE PRECIFICAÇÃO DA VIAGEM (BOTÕES + / - E BARRA DE DESLIZAR SLIDER)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "💰 PREÇO OFERECIDO NA VIAGEM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Botão - (Diminuir)
                                IconButton(
                                    onClick = {
                                        if (offeredPriceAmount > 10.0f) {
                                            offeredPriceAmount = (offeredPriceAmount - 2.0f).coerceAtLeast(10.0f)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                ) {
                                    Text(text = "−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = String.format("R$ %.2f", offeredPriceAmount).replace('.', ','),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = FontFamily.Serif
                                    )
                                    Text(
                                        text = "Ajuste com + / - ou deslize a barra abaixo",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Botão + (Aumentar)
                                IconButton(
                                    onClick = {
                                        if (offeredPriceAmount < 80.0f) {
                                            offeredPriceAmount = (offeredPriceAmount + 2.0f).coerceAtMost(80.0f)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                ) {
                                    Text(text = "+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Barra de Deslizar (Slider para esquerda e direita)
                            Slider(
                                value = offeredPriceAmount,
                                onValueChange = { offeredPriceAmount = it },
                                valueRange = 10.0f..60.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Mín: R$ 10,00", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "Deslize ◄ ►", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(text = "Máx: R$ 60,00", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Título de escolha de viagem
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Escolha sua viagem",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Valores estimados",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "📅", fontSize = 12.sp)
                                Text(text = "Agora", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Lista de Categorias de Corrida
                    categories.forEachIndexed { index, cat ->
                        val isSelected = (index == selectedCategoryIndex)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    selectedCategoryIndex = index
                                    offeredPriceAmount = when (index) {
                                        0 -> 18.40f // Carro
                                        1 -> 24.90f // Carro com Ar
                                        2 -> 12.00f // Moto
                                        3 -> 14.50f // Entregas
                                        4 -> 45.00f // Frete
                                        else -> 18.40f
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(text = cat.icon, fontSize = 26.sp)
                                    Column {
                                        Text(
                                            text = cat.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = cat.timeAndType,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = cat.price,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(text = "👤", fontSize = 11.sp)
                                        Text(
                                            text = "${cat.capacity}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Método de Pagamento
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "💳", fontSize = 16.sp)
                            Column {
                                Text(text = "Pagamento", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "Visa •••• 2048",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Text(text = "›", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botão Principal "Confirmar [Categoria] >"
                    val currentCatName = categories[selectedCategoryIndex].name.removePrefix("Caramelo ")
                    val currentDriverPref = driverPreferences[selectedDriverPrefIndex].title
                    val formattedOfferedPrice = String.format("R$ %.2f", offeredPriceAmount).replace('.', ',')
                    Button(
                        onClick = {
                            rideStatus = RideStatus.SEARCHING_DRIVER
                            onRideRequested(destination, categories[selectedCategoryIndex].name, currentDriverPref)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Confirmar $currentCatName por $formattedOfferedPrice",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(text = "›", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rodapé de Segurança
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🛡️ ", fontSize = 12.sp)
                        Text(
                            text = "Viagens protegidas do início ao fim",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Drawer / Menu Lateral da Marina Costa
@Composable
fun CarameloDrawerContent(
    onClose: () -> Unit,
    onOpenTripHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPresentation: () -> Unit,
    onToast: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar com fechar 'X', Logo, Pitch, MC
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Text(text = "✕", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }

                CarameloLogo()

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "📊", fontSize = 12.sp)
                            Text(
                                text = "Pitch",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(CarameloRustDark, CircleShape)
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MC",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Informações do Usuário
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(CarameloRustDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MC",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Column {
                    Text(
                        text = "Marina Costa",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "4,92 de avaliação ★",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Itens de Menu
            val menuItems = listOf(
                Triple("🕒", "Suas viagens", onOpenTripHistory),
                Triple("⚙️", "Configurações", onOpenSettings),
                Triple("💳", "Pagamentos", { onToast("Formas de pagamento abertas") }),
                Triple("🛡️", "Segurança", { onToast("Central de Segurança") }),
                Triple("👥", "Convide amigos", { onToast("Convide e Ganhe R$ 10") })
            )

            menuItems.forEach { (emoji, title, action) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { action() }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(text = "›", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Card Promocional "Indique o Caramelo"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CarameloRustDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "GANHE R$ 10",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CarameloYellow,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Indique o Caramelo para alguém especial.",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onToast("Link de indicação copiado!") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CarameloYellow)
                    ) {
                        Text(
                            text = "Convidar agora",
                            color = CarameloRustDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botão Ver Apresentação Comercial
            OutlinedButton(
                onClick = onOpenPresentation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "📊", fontSize = 16.sp)
                    Text(
                        text = "Ver apresentação comercial",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// TELA DE HISTÓRICO DE VIAGENS
data class TripHistoryItem(
    val date: String,
    val origin: String,
    val destination: String,
    val driverName: String,
    val driverRating: String,
    val category: String,
    val driverType: String,
    val price: String
)

@Composable
fun CarameloTripHistoryScreen(onClose: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val historyItems = remember { CarameloRepository.getTripHistory(context) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CarameloLogo()

                OutlinedButton(
                    onClick = onClose,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = "✕ Voltar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "— SEU HISTÓRICO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Text(
                text = "Suas Viagens",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(16.dp))

            historyItems.forEach { trip ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = trip.date,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CarameloGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "✓ Concluída",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CarameloGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "de: ${trip.origin}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "para: ${trip.destination}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Motorista: ${trip.driverName} (${trip.driverRating})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${trip.category} • Motorista: ${trip.driverType}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = trip.price,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// Apresentação Comercial (Slide 01/07) - Mobilidade com calor humano
@Composable
fun CarameloPresentationScreen(onClose: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header do Slide
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CarameloLogo()

                Text(
                    text = "01 / 07",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = onClose,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = "✕ Voltar ao app",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Conteúdo principal do slide
            Text(
                text = "APRESENTAÇÃO COMERCIAL · 2025",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Mobilidade\ncom calor\nhumano.",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Serif,
                lineHeight = 48.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Uma plataforma brasileira de mobilidade urbana criada para transformar trajetos cotidianos em experiências mais simples, seguras e próximas.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Diagrama com círculos concêntricos e ícone de carro
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(CarameloYellow.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(CarameloYellow.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🚖", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "caramelo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }
            }
        }
    }
}

// Previews do Compose
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MapaoDoOdaPreview() {
    Mapa_testeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            MainAppContent()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MapaoDoOdaDarkPreview() {
    Mapa_testeTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            MainAppContent()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TripHistoryPreview() {
    Mapa_testeTheme {
        CarameloTripHistoryScreen(onClose = {})
    }
}
