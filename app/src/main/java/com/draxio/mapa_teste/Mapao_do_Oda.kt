package com.draxio.mapa_teste

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Map as MapIcon
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.draxio.mapa_teste.ui.components.*
import com.draxio.mapa_teste.ui.theme.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class Mapao_do_Oda : FragmentActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isFirstZoom = true

    private val locationPermissionRequestCode = 1
    // A permissao e pedida uma unica vez por criacao da Activity
    private var permissionRequested = false
    // Permissao negada: mapa centrado em Sao Paulo e partida sem "Localizacao atual"
    private var locationDenied by mutableStateOf(false)

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun centerOnSaoPaulo() {
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-23.5614, -46.6559), 13f))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("location_denied", locationDenied)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        locationDenied = savedInstanceState?.getBoolean("location_denied", false) ?: false

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
                        },
                        locationDenied = locationDenied
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

        // Evita registrar o mesmo callback duas vezes (onResume + enableMyLocation)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            locationDenied = false
            mMap?.isMyLocationEnabled = true
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (hasLocationPermission()) {
            locationDenied = false
            mMap?.isMyLocationEnabled = true
            startLocationUpdates()
            return
        }

        if (locationDenied) {
            // Ja negada: nao pede de novo; mapa em Sao Paulo
            centerOnSaoPaulo()
            return
        }
        if (permissionRequested) return
        permissionRequested = true
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            locationPermissionRequestCode
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) { // any já cobre array vazio
                enableMyLocation()
            } else {
                locationDenied = true
                centerOnSaoPaulo()
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Logo oficial em versao de cabecalho: pino com o caramelo ao volante + nome.
// Usado em todas as telas; o logo completo (com o nome desenhado) fica no Login.
@Composable
fun CarameloLogo(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background == CarameloDarkBackground
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.caramelo_marca),
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Text(
            text = "Caramelo",
            color = if (isDark) CarameloOrange else CarameloRustDark,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            fontFamily = CarameloBrandFont,
            letterSpacing = (-0.3).sp
        )
    }
}

@Composable
fun MainAppContent(
    onMapReady: (GoogleMap) -> Unit = {},
    onToast: (String) -> Unit = {},
    locationDenied: Boolean = false
) {
    var isDrawerOpen by rememberSaveable { mutableStateOf(false) }
    var showPresentation by rememberSaveable { mutableStateOf(false) }
    var showTripHistory by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var isRideActive by rememberSaveable { mutableStateOf(false) }
    var showWallet by rememberSaveable { mutableStateOf(false) }
    var showHelp by rememberSaveable { mutableStateOf(false) }

    // Voltar do sistema: fecha a tela sobreposta de cima ou o drawer
    BackHandler(enabled = isDrawerOpen || showPresentation || showTripHistory || showSettings || showWallet || showHelp) {
        when {
            showWallet -> showWallet = false
            showHelp -> showHelp = false
            showPresentation -> showPresentation = false
            showTripHistory -> showTripHistory = false
            showSettings -> showSettings = false
            isDrawerOpen -> isDrawerOpen = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa base (sempre composto: o fragmento nao e recriado ao abrir outras telas)
        MapScreen(onMapReady = onMapReady)

        // Topo do mapa: menu redondo a esquerda e selo "12 motoristas por perto" a direita
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CarameloCircleButton(
                icon = Icons.Rounded.Menu,
                contentDescription = "Abrir menu",
                onClick = { isDrawerOpen = true }
            )
            CarameloStatusPill(
                text = "12 motoristas por perto",
                dotColor = CarameloGreen,
                dark = true
            )
        }

        // Painel Principal de solicitação de viagem (RideHailingOverlay)
        RideHailingOverlay(
            onOpenDrawer = { isDrawerOpen = true },
            onRideRequested = { dest, cat, driverPref ->
                onToast("Buscando $cat ($driverPref) para $dest...")
            },
            bottomContentPadding = CarameloNavBarClearance,
            onStatusChange = { status -> isRideActive = status != RideStatus.IDLE },
            onOpenWallet = { showWallet = true },
            paymentRefreshKey = showWallet,
            backEnabled = !isDrawerOpen && !showWallet && !showHelp &&
                !showPresentation && !showTripHistory && !showSettings,
            locationDenied = locationDenied
        )

        // Menu inferior flutuante: so aparece com o painel ocioso (some durante a busca/corrida e com o menu lateral aberto)
        if (!isRideActive && !isDrawerOpen) {
            CarameloFloatingNavBar(
                selected = CarameloNavItem.Inicio,
                onSelect = { item ->
                    when (item) {
                        CarameloNavItem.Inicio -> { }
                        CarameloNavItem.Viagens -> { showTripHistory = true }
                        CarameloNavItem.Perfil -> { showSettings = true }
                        CarameloNavItem.Carteira -> { showWallet = true }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            )
        }

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
                onToast = onToast,
                onOpenWallet = {
                    isDrawerOpen = false
                    showWallet = true
                },
                onOpenHelp = {
                    isDrawerOpen = false
                    showHelp = true
                }
            )
        }

        // Telas sobrepostas (mapa e painel da corrida continuam vivos por baixo)
        if (showWallet) {
            WalletScreen(
                onClose = { showWallet = false },
                onToast = onToast
            )
        }
        if (showHelp) {
            HelpScreen(onClose = { showHelp = false })
        }
        if (showPresentation) {
            FullScreenOverlay {
                CarameloPresentationScreen(onClose = { showPresentation = false })
            }
        }
        if (showTripHistory) {
            FullScreenOverlay {
                CarameloTripHistoryScreen(onClose = { showTripHistory = false })
            }
        }
        if (showSettings) {
            FullScreenOverlay {
                SettingsScreen(
                    onClose = { showSettings = false },
                    onToast = onToast
                )
            }
        }
    }
}

// Tela cheia opaca por cima do mapa e do painel; bloqueia toques para o que esta por baixo
@Composable
private fun FullScreenOverlay(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {}
    ) {
        content()
    }
}

private const val MAP_FRAGMENT_TAG = "caramelo_map_fragment"
private const val MAP_CONTAINER_ID = 0x00CA7E10

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
                Icon(
                    imageVector = Icons.Rounded.MapIcon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mapa do Caramelo",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    } else {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val fragmentActivity = context as FragmentActivity
                androidx.fragment.app.FragmentContainerView(context).apply {
                    // Id fixo: o fragmento restaurado depois da rotacao encontra o mesmo container
                    id = MAP_CONTAINER_ID
                    tag = "map_fragment_container"
                    val fm = fragmentActivity.supportFragmentManager
                    val existing = fm.findFragmentByTag(MAP_FRAGMENT_TAG) as? SupportMapFragment
                    val mapFragment = if (existing != null && existing.view != null) {
                        existing
                    } else {
                        // Sem instancia utilizavel: remove a antiga (se houver) e cria uma unica com a tag fixa
                        val tx = fm.beginTransaction()
                        if (existing != null) tx.remove(existing)
                        val created = SupportMapFragment.newInstance()
                        tx.add(this.id, created, MAP_FRAGMENT_TAG).commit()
                        created
                    }
                    mapFragment.getMapAsync { googleMap ->
                        onMapReady(googleMap)
                    }
                }
            },
            update = { _ -> }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHailingOverlay(
    onOpenDrawer: () -> Unit,
    onRideRequested: (String, String, String) -> Unit,
    bottomContentPadding: Dp = 0.dp,
    onStatusChange: (RideStatus) -> Unit = {},
    onOpenWallet: () -> Unit = {},
    paymentRefreshKey: Any = Unit,
    backEnabled: Boolean = true,
    locationDenied: Boolean = false
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var rideStatus by rememberSaveable { mutableStateOf(RideStatus.IDLE) }
    var boardingPin by rememberSaveable { mutableStateOf("") }
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }
    var showCallDialog by rememberSaveable { mutableStateOf(false) }
    var showChatDialog by rememberSaveable { mutableStateOf(false) }
    var showDestinationPicker by rememberSaveable { mutableStateOf(false) }
    val chatMessages = remember { mutableStateListOf<ChatLine>() }
    val chatScope = rememberCoroutineScope()
    // Forma de pagamento e cupom: relidos quando a Carteira fecha
    val paymentMethod = remember(paymentRefreshKey) { CarameloRepository.getPaymentMethod(context) }
    val activeCoupon = remember(paymentRefreshKey) { CarameloRepository.getActiveCoupon(context) }
    // Nome e iniciais do usuario
    val userName = remember { CarameloRepository.getUserName(context) }
    val userInitials = remember(userName) { userInitialsOf(userName) }
    // Passo do botao +/- (ex.: "R$ 2,00" -> 2.0f), com fallback 2.0f
    val priceStep = remember {
        CarameloRepository.getPriceIncrementStep(context)
            .filter { it.isDigit() || it == ',' || it == '.' }
            .replace(',', '.')
            .toFloatOrNull()?.takeIf { it > 0f } ?: 2.0f
    }
    val priceMin = 10.0f
    val priceMax = 80.0f
    // Avisa quem esta por fora (ex.: esconder o menu flutuante durante a busca/corrida)
    val latestOnStatusChange by rememberUpdatedState(onStatusChange)
    LaunchedEffect(rideStatus) { latestOnStatusChange(rideStatus) }
    var destination by rememberSaveable { mutableStateOf("Shopping Cidade Jardim") }
    val origin = if (locationDenied) "Defina o ponto de partida" else "Localização atual"

    // Tipo de Motorista Escolhido
    val driverPreferences = remember {
        listOf(
            DriverPreferenceOption("Geral", "Geral", "👤"),
            DriverPreferenceOption("LGBTQIA+", "LGBTQIA+", "🏳️‍🌈"),
            DriverPreferenceOption("Mulher", "Mulher", "👩"),
            DriverPreferenceOption("Homem", "Homem", "👨")
        )
    }
    // Comeca na preferencia salva so se "Fixar" estiver ligado; senao "Geral"
    var selectedDriverPrefIndex by rememberSaveable {
        mutableIntStateOf(
            if (CarameloRepository.isFixedPreferenceEnabled(context)) {
                val savedId = CarameloRepository.getDefaultDriverPreference(context)
                driverPreferences.indexOfFirst { it.id == savedId }.coerceAtLeast(0)
            } else 0
        )
    }
    var isFixedDriverPreference by rememberSaveable {
        mutableStateOf(CarameloRepository.isFixedPreferenceEnabled(context))
    }

    // Oferta de Preço com + e - e Slider (Deslizar)
    var offeredPriceAmount by rememberSaveable { mutableFloatStateOf(18.40f) }

    // Stream de Propostas dos Motoristas: precos derivados do valor oferecido no pedido
    // (guardado com rememberSaveable para a lista ser estavel e sobreviver a rotacao)
    var requestedPrice by rememberSaveable { mutableFloatStateOf(0f) }
    val initialDriverOffers = remember(requestedPrice) {
        buildDriverOffers(if (requestedPrice > 0f) requestedPrice else offeredPriceAmount)
    }
    val currentOffers = remember(requestedPrice) { mutableStateListOf<DriverOffer>().apply { addAll(initialDriverOffers) } }
    // Motorista aceito: so o id sobrevive a rotacao; o DriverOffer e derivado da lista
    var acceptedOfferId by rememberSaveable { mutableStateOf<String?>(null) }
    val acceptedOffer = acceptedOfferId?.let { id -> initialDriverOffers.firstOrNull { it.id == id } }
    // Motorista cuja ficha de segurança está aberta (null = fechada)
    var safetyOffer by remember { mutableStateOf<DriverOffer?>(null) }
    var safetyDuringTrip by remember { mutableStateOf(false) }
    var chatJob by remember { mutableStateOf<Job?>(null) }
    // Bloqueados: lista lida quando a busca comeca
    val blockedNames = remember(rideStatus == RideStatus.SEARCHING_DRIVER) {
        if (rideStatus == RideStatus.SEARCHING_DRIVER) {
            CarameloRepository.getBlockedDrivers(context).map { it.name.trim().lowercase() }.toSet()
        } else emptySet()
    }
    val activeDriver = acceptedOffer ?: initialDriverOffers.first()
    val openSafety = {
        safetyDuringTrip = true
        safetyOffer = activeDriver
    }
    val openRoute = {
        val geo = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            "geo:0,0?q=${android.net.Uri.encode(destination)}".toUri()
        )
        try {
            context.startActivity(geo)
        } catch (_: android.content.ActivityNotFoundException) {
            Toast.makeText(context, "Nenhum app de mapas encontrado", Toast.LENGTH_SHORT).show()
        }
    }
    val finishRide = {
        rideStatus = RideStatus.IDLE
        acceptedOfferId = null
        boardingPin = ""
        chatJob?.cancel()
        chatJob = null
        chatMessages.clear()
        showCancelDialog = false
        showChatDialog = false
        showCallDialog = false
        safetyOffer = null
    }
    // Voltar do sistema: busca volta a IDLE; a caminho/em viagem pede o motivo; recibo exige "Concluir"
    BackHandler(enabled = backEnabled && rideStatus != RideStatus.IDLE) {
        when (rideStatus) {
            RideStatus.SEARCHING_DRIVER -> finishRide()
            RideStatus.DRIVER_ARRIVING, RideStatus.IN_TRIP -> showCancelDialog = true
            else -> {}
        }
    }
    // Corrida em andamento sem motorista valido (ex.: estado restaurado): volta a IDLE
    LaunchedEffect(rideStatus, acceptedOffer) {
        if (rideStatus != RideStatus.IDLE && rideStatus != RideStatus.SEARCHING_DRIVER && acceptedOffer == null) {
            finishRide()
        }
    }

    val categories = remember {
        listOf(
            RideCategory("Carro", "R$ 18,40", "3 min • Carro Econômico", 4, "🚗"),
            RideCategory("Carro com Ar", "R$ 24,90", "4 min • Ar-Condicionado Gelando", 4, "❄️"),
            RideCategory("Moto", "R$ 12,00", "2 min • Viagem Rápida e Ágil", 1, "🏍️"),
            RideCategory("Entregas", "R$ 14,50", "3 min • Pacotes e Encomendas", 1, "📦"),
            RideCategory("Frete", "R$ 45,00", "8 min • Transporte de Cargas", 2, "🚚")
        )
    }
    var selectedCategoryIndex by rememberSaveable { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Bottom Sheet / Painel principal (branco, cantos de 32 dp no topo, alca de arrasto)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp + bottomContentPadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Alca de arrasto
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 40.dp, height = 5.dp)
                        .background(MaterialTheme.colorScheme.outline, CircleShape)
                )

                // Cabecalho do painel: logo e avatar (abre o menu lateral)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CarameloLogo()
                    Surface(
                        onClick = onOpenDrawer,
                        shape = CircleShape,
                        color = carameloDarkFill(),
                        contentColor = Color.White,
                        modifier = Modifier
                            .size(44.dp)
                            .semantics { contentDescription = "Abrir menu do perfil" }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = userInitials, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                CarameloScreenTitle(title = "Para onde vamos?", overline = "Sua cidade, seu caminho")

                // Campo de partida e destino ("Buscar destino")
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Partida",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = origin,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(start = 32.dp, top = 10.dp, bottom = 10.dp),
                            color = MaterialTheme.colorScheme.outline
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDestinationPicker = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Buscar destino",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = destination,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.Place,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Titulo de escolha de viagem
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Escolha sua viagem",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Valores estimados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    CarameloChip(
                        label = "Agora",
                        selected = false,
                        onClick = { },
                        icon = Icons.Rounded.Schedule
                    )
                }

                // Lista de Categorias de Corrida (cartoes selecionaveis)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEachIndexed { index, cat ->
                        val isSelected = (index == selectedCategoryIndex)
                        Surface(
                            onClick = {
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(14.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = rideCategoryIcon(cat.name),
                                        contentDescription = null,
                                        modifier = Modifier.size(26.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = cat.timeAndType,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = cat.price,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Person,
                                            contentDescription = "Lugares",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${cat.capacity}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // SEÇÃO DE SELEÇÃO DE TIPO DE MOTORISTA (Geral, LGBTQIA+, Mulher, Homem)
                CarameloSectionLabel(text = "Tipo de motorista preferido")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    driverPreferences.forEachIndexed { idx, opt ->
                        CarameloChip(
                            label = opt.title,
                            selected = idx == selectedDriverPrefIndex,
                            onClick = {
                                selectedDriverPrefIndex = idx
                                // Com "Fixar" ligado, a nova escolha vira o padrao
                                if (isFixedDriverPreference) {
                                    CarameloRepository.saveDefaultDriverPreference(context, opt.id)
                                }
                            },
                            icon = driverPreferenceIcon(opt.id)
                        )
                    }
                }

                // Opção de Preferência de Viagem Fixa
                CarameloSwitchRow(
                    icon = Icons.Rounded.CheckCircle,
                    title = "Fixar esta preferência para todas as viagens",
                    checked = isFixedDriverPreference,
                    onCheckedChange = { checked ->
                        isFixedDriverPreference = checked
                        CarameloRepository.saveFixedPreferenceEnabled(context, checked)
                        if (checked) {
                            CarameloRepository.saveDefaultDriverPreference(
                                context,
                                driverPreferences[selectedDriverPrefIndex].id
                            )
                        }
                    }
                )

                // CARD DE PRECIFICAÇÃO DA VIAGEM (BOTÕES + / - E BARRA DE DESLIZAR SLIDER)
                CarameloCard(color = MaterialTheme.colorScheme.surfaceVariant) {
                    CarameloSectionLabel(text = "Preço oferecido na viagem")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botão - (Diminuir)
                        CarameloCircleButton(
                            icon = Icons.Rounded.Remove,
                            contentDescription = "Diminuir oferta",
                            onClick = {
                                offeredPriceAmount = (offeredPriceAmount - priceStep).coerceIn(priceMin, priceMax)
                            },
                            style = CircleStyle.Outline
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = formatBrl(offeredPriceAmount),
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ajuste com + / - ou deslize a barra",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Botão + (Aumentar)
                        CarameloCircleButton(
                            icon = Icons.Rounded.Add,
                            contentDescription = "Aumentar oferta",
                            onClick = {
                                offeredPriceAmount = (offeredPriceAmount + priceStep).coerceIn(priceMin, priceMax)
                            },
                            style = CircleStyle.Primary
                        )
                    }

                    // Barra de Deslizar (Slider para esquerda e direita)
                    Slider(
                        value = offeredPriceAmount,
                        onValueChange = { offeredPriceAmount = it },
                        valueRange = priceMin..priceMax,
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
                        Text(text = "Mín: ${formatBrl(priceMin)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Máx: ${formatBrl(priceMax)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Método de Pagamento
                CarameloListRow(
                    icon = Icons.Rounded.CreditCard,
                    title = paymentLabel(paymentMethod),
                    subtitle = if (activeCoupon != null) "Pagamento · Cupom $activeCoupon" else "Pagamento",
                    onClick = onOpenWallet,
                    trailing = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                // Botão Principal "Confirmar [Categoria] por R$ ..."
                val currentCatName = categories[selectedCategoryIndex].name.removePrefix("Caramelo ")
                val currentDriverPref = driverPreferences[selectedDriverPrefIndex].title
                val formattedOfferedPrice = formatBrl(offeredPriceAmount)
                CarameloPillButton(
                    text = "Confirmar $currentCatName por $formattedOfferedPrice",
                    onClick = {
                        // Propostas geradas a partir do valor oferecido neste momento
                        requestedPrice = offeredPriceAmount
                        currentOffers.clear()
                        currentOffers.addAll(buildDriverOffers(offeredPriceAmount))
                        rideStatus = RideStatus.SEARCHING_DRIVER
                        onRideRequested(destination, categories[selectedCategoryIndex].name, currentDriverPref)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    style = PillStyle.Primary
                )

                // Rodapé de Segurança
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CarameloGreen
                    )
                    Text(
                        text = "Viagens protegidas do início ao fim",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Overlay de busca (Stream de Propostas) e motorista a caminho - desenhado por cima do painel
        if (rideStatus != RideStatus.IDLE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { },
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 20.dp, end = 20.dp, top = 10.dp)
                            .navigationBarsPadding()
                            .padding(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Alca de arrasto
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(width = 40.dp, height = 5.dp)
                                .background(MaterialTheme.colorScheme.outline, CircleShape)
                        )

                        if (rideStatus == RideStatus.SEARCHING_DRIVER) {
                            // Só motoristas verificados e do tipo escolhido ("Geral" = qualquer um)
                            val chosenGroup = driverPreferences[selectedDriverPrefIndex].id
                            val visibleOffers = currentOffers.filter {
                                it.verified &&
                                    (chosenGroup == "Geral" || it.groupName() == chosenGroup) &&
                                    it.driverName.trim().lowercase() !in blockedNames
                            }

                            // Cabecalho: titulo e botao de fechar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    CarameloSectionLabel(text = "Propostas dos motoristas (${visibleOffers.size})")
                                    Text(
                                        text = "Motoristas aceitando viagens",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                CarameloCircleButton(
                                    icon = Icons.Rounded.Close,
                                    contentDescription = "Fechar",
                                    onClick = { finishRide() },
                                    style = CircleStyle.Outline
                                )
                            }

                            CarameloSegmentedProgress(progress = 0.4f, height = 10.dp)

                            // Selo de segurança + filtro em uso
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.VerifiedUser,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = CarameloGreen
                                )
                                Text(
                                    text = if (chosenGroup == "Geral")
                                        "Todos os motoristas passaram pela checagem de antecedentes e identidade"
                                    else
                                        "Só motoristas do grupo $chosenGroup, todos com antecedentes e identidade checados",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "Toque na proposta para remover/recusar ou clique em Aceitar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (visibleOffers.isEmpty()) {
                                CarameloCard(color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text(
                                        text = "Buscando novas propostas...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    CarameloPillButton(
                                        text = "Restaurar Propostas",
                                        onClick = {
                                            currentOffers.clear()
                                            currentOffers.addAll(initialDriverOffers)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 320.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    visibleOffers.forEach { offer ->
                                        // Removível ao toque direto no card
                                        Surface(
                                            onClick = { currentOffers.remove(offer) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    OdaInitialsAvatar(name = offer.driverName, size = 44.dp)
                                                    Column(
                                                        modifier = Modifier.weight(1f),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = offer.driverName,
                                                            style = MaterialTheme.typography.titleSmall,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = "${offer.vehicle} · ${offer.rating}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            OdaPlateBadge(plate = offer.plate)
                                                            OdaVerifiedBadge(onClick = {
                                                                safetyDuringTrip = false
                                                                safetyOffer = offer
                                                            })
                                                        }
                                                    }
                                                    Text(
                                                        text = offer.offeredPrice,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = "${offer.driverTypeTag.substringAfter(' ')} · ${offer.eta}",
                                                        modifier = Modifier.weight(1f),
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    CarameloCircleButton(
                                                        icon = Icons.Rounded.Close,
                                                        contentDescription = "Recusar proposta de ${offer.driverName}",
                                                        onClick = { currentOffers.remove(offer) },
                                                        style = CircleStyle.Outline,
                                                        size = 44.dp
                                                    )
                                                    CarameloPillButton(
                                                        text = "Aceitar",
                                                        onClick = {
                                                            acceptedOfferId = offer.id
                                                            // Código de 4 dígitos para o embarque; o histórico só grava no fim
                                                            boardingPin = kotlin.random.Random.nextInt(1000, 10000).toString()
                                                            chatMessages.clear()
                                                            rideStatus = RideStatus.DRIVER_ARRIVING
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            CarameloPillButton(
                                text = "Cancelar busca",
                                onClick = { finishRide() },
                                modifier = Modifier.fillMaxWidth(),
                                style = PillStyle.Outline
                            )
                        } else {
                            when (rideStatus) {
                                RideStatus.DRIVER_ARRIVING -> RideArrivingContent(
                                    driver = activeDriver,
                                    pin = boardingPin,
                                    onBoarded = { rideStatus = RideStatus.IN_TRIP },
                                    onCancel = { showCancelDialog = true },
                                    onCall = { showCallDialog = true },
                                    onChat = { showChatDialog = true },
                                    onRoute = openRoute,
                                    onSafety = openSafety
                                )
                                RideStatus.IN_TRIP -> RideInTripContent(
                                    driver = activeDriver,
                                    destination = destination,
                                    onArrived = { rideStatus = RideStatus.COMPLETED },
                                    onCancel = { showCancelDialog = true },
                                    onCall = { showCallDialog = true },
                                    onChat = { showChatDialog = true },
                                    onRoute = openRoute,
                                    onSafety = openSafety,
                                    paused = showCancelDialog
                                )
                                else -> RideReceiptContent(
                                    driver = activeDriver,
                                    destination = destination,
                                    onFinish = { rating, tipText, fare, payment ->
                                        saveCompletedTrip(
                                            context, activeDriver, origin, destination,
                                            categories[selectedCategoryIndex].name,
                                            fare, rating, tipText, payment
                                        )
                                        // O cupom vale para uma corrida só
                                        if (CarameloRepository.getActiveCoupon(context) != null) {
                                            CarameloRepository.clearCoupon(context)
                                        }
                                        finishRide()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Destino editável
        if (showDestinationPicker) {
            DestinationPickerSheet(
                current = destination,
                onPick = {
                    destination = it
                    showDestinationPicker = false
                },
                onDismiss = { showDestinationPicker = false }
            )
        }

        // Cancelamento com motivo (a caminho ou em viagem)
        if (showCancelDialog && (rideStatus == RideStatus.DRIVER_ARRIVING || rideStatus == RideStatus.IN_TRIP)) {
            CancelRideDialog(
                onConfirm = {
                    saveCanceledTrip(
                        context, activeDriver, origin, destination,
                        categories[selectedCategoryIndex].name, paymentLabel(paymentMethod)
                    )
                    showCancelDialog = false
                    finishRide()
                },
                onDismiss = { showCancelDialog = false }
            )
        }

        // Chat simulado com o motorista
        if (showChatDialog) {
            RideChatDialog(
                driverName = activeDriver.driverName,
                messages = chatMessages,
                onSend = { text ->
                    chatMessages.add(ChatLine(true, text))
                    chatJob = chatScope.launch {
                        delay(1500)
                        chatMessages.add(ChatLine(false, "Ok, combinado!"))
                    }
                },
                onDismiss = { showChatDialog = false }
            )
        }

        if (showCallDialog) {
            CallDemoDialog(onDismiss = { showCallDialog = false })
        }

        // Ficha de segurança do motorista (selo "Verificado" ou ação Segurança)
        val safetyTarget = safetyOffer
        if (safetyTarget != null) {
            val safetyContext = androidx.compose.ui.platform.LocalContext.current
            DriverSafetyDialog(
                offer = safetyTarget,
                duringTrip = safetyDuringTrip,
                onDismiss = { safetyOffer = null },
                onCallEmergency = {
                    // ACTION_DIAL só abre o discador com o número; não liga sozinho e não pede permissão
                    val dial = android.content.Intent(android.content.Intent.ACTION_DIAL, "tel:190".toUri())
                    try {
                        safetyContext.startActivity(dial)
                    } catch (_: android.content.ActivityNotFoundException) {
                        Toast.makeText(safetyContext, "Nenhum app de telefone encontrado. Ligue 190.", Toast.LENGTH_LONG).show()
                    }
                },
                onShareTrip = {
                    val text = "Estou numa viagem Caramelo com ${safetyTarget.driverName}, " +
                        "${safetyTarget.vehicle} placa ${safetyTarget.plate}, indo para $destination."
                    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, text)
                    }
                    safetyContext.startActivity(android.content.Intent.createChooser(send, "Compartilhar viagem"))
                }
            )
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
