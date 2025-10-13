package com.urbansetu.app.brands

// ---------- Android ----------
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast

// ---------- Compose runtime / state ----------
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateMapOf

// ---------- Compose UI / foundation / material ----------
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn


// ---------- Google Location ----------
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

// ---------- Other ----------
import com.urbansetu.app.R
import com.urbansetu.app.analytics.AnalyticsRepo
import com.urbansetu.app.wallet.WalletViewModel
import kotlinx.coroutines.delay

// ==================== Location helper ====================
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay

@Composable
private fun rememberUserLocation(): Pair<Double, Double>? {
    val context = androidx.compose.ui.platform.LocalContext.current
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    fun fetch() {
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                userLocation = loc.latitude to loc.longitude
            } else {
                client.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { loc2 ->
                    if (loc2 != null) {
                        userLocation = loc2.latitude to loc2.longitude
                    }
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetch()
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) fetch() else launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    return userLocation
}

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    val c = 2 * Math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return R * c
}

// ==================== Tiny UI bits (badges / metrics) ====================

@Composable
private fun BadgesRow(b: Brand) {
    val analytics by AnalyticsRepo.all.collectAsState()
    val m = analytics[b.id]
    val trending = (m?.clicks ?: 0) >= 5

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (trending) AssistChip(onClick = {}, enabled = false, label = { Text("Trending") })
        if (b.isNew) AssistChip(onClick = {}, enabled = false, label = { Text("New") })
        if (isExpiringSoon(b.validity)) {
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("Expiring soon") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            )
        }
    }
}

private fun isExpiringSoon(validity: String): Boolean =
    validity.contains("today", ignoreCase = true)

@Composable
private fun MetricsBadge(brandId: String) {
    val all by AnalyticsRepo.all.collectAsState()
    val m = all[brandId] ?: return
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text("Views ${m.impressions} · Clicks ${m.clicks} · Redeems ${m.redemptions}") }
    )
}

@Composable
private fun DistanceLine(b: Brand) {
    b.fakeDistanceMeters?.let { d ->
        Text(
            "$d m away",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

// ==================== Cards ====================

@Composable
private fun BrandWideCard(b: Brand, onClick: (Brand) -> Unit) {
    LaunchedEffect(b.id) { AnalyticsRepo.trackImpressionOnce(b.id) }
    Card(
        onClick = { AnalyticsRepo.trackClick(b.id); onClick(b) },
        modifier = Modifier.width(260.dp).height(132.dp).padding(end = 10.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            Text(b.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(b.headline, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(b.subtext, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(6.dp)); DistanceLine(b)
            Spacer(Modifier.height(6.dp)); BadgesRow(b)
            Spacer(Modifier.height(6.dp)); MetricsBadge(brandId = b.id)
        }
    }
}

@Composable
private fun BrandTile(
    b: Brand,
    onClick: (Brand) -> Unit,
    onEdit: (Brand) -> Unit = {},
    currentUserId: String? = null,
    openOwnerPanel: (Brand) -> Unit = {},
    onManage: (Brand) -> Unit   // 👈 NEW
) {
    LaunchedEffect(b.id) { AnalyticsRepo.trackImpressionOnce(b.id) }
    var showEditor by rememberSaveable { mutableStateOf(false) }

    Card(onClick = { AnalyticsRepo.trackClick(b.id); onClick(b) }) {
        Column(Modifier.padding(12.dp)) {
            // ... your existing header, copy, buttons, badges, metrics ...

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { showEditor = true }) { Text("Edit radius") }

            // ✅ Show “Manage” only for the owner
            if (currentUserId != null && currentUserId == b.ownerId) {
                Spacer(Modifier.height(6.dp))
                FilledTonalButton(onClick = { onManage(b) }) { Text("Manage") }
            }
        }
    }

    if (showEditor) {
        BrandOwnerRadiusEditor(
            b = b,
            onSave = { updated -> onEdit(updated); showEditor = false },
            onClose = { showEditor = false }
        )
    }
}


// ==================== Hero rotation ====================

@Composable
private fun HeroRotating(banners: List<Brand>) {
    if (banners.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { banners.size })
    val scope = rememberCoroutineScope()
    DisposableEffect(banners.size) {
        val job = scope.launch {
            while (isActive && pagerState.pageCount > 0) {
                delay(2800)
                val next = (pagerState.currentPage + 1) % pagerState.pageCount
                pagerState.animateScrollToPage(next)
            }
        }
        onDispose { job.cancel() }
    }
    Surface(
        modifier = Modifier.fillMaxWidth().height(160.dp).clip(MaterialTheme.shapes.large)
    ) {
        HorizontalPager(state = pagerState) { page ->
            val b = banners[page % banners.size]
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            ) {
                Column(Modifier.align(Alignment.CenterStart)) {
                    Text(b.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(b.headline, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    AssistChip(
                        onClick = { /* no-op */ },
                        label = { Text("View offer") },
                        leadingIcon = { Icon(Icons.Filled.LocalOffer, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    )
                }
                Image(
                    painter = painterResource(b.logoRes),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).align(Alignment.BottomEnd)
                )
            }
        }
    }
}

// ==================== Filters row ====================

@Composable
fun FilterRow(categories: List<String>, selected: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(selected = category == selected, onClick = { onSelected(category) }, label = { Text(category) })
        }
    }
}

// ==================== Screen ====================

@Composable
fun BrandsScreen(
    onBrandClick: (Brand) -> Unit = {},
    walletViewModel: WalletViewModel
) {
    LaunchedEffect(Unit) {
        if (BrandRepo.isEmpty()) BrandRepo.resetWithSamples()
    }
    // demo identity; wire to auth later
    val currentUserId = remember { "owner_1" }

    // ⭐ Observe the repo (single source of truth)
    val brands by BrandRepo.brands.collectAsState()

    // ...use `brands` everywhere instead of a local list:

    // single source of truth for brands (starts with samples)
    val categories = listOf("All", "Cafés", "Supermarkets", "Fintech", "Mobility")

    // UI state
    var selected by rememberSaveable { mutableStateOf("All") }
    var nearYouOnly by rememberSaveable { mutableStateOf(false) }
    var selectedBrandForOwner by remember { mutableStateOf<Brand?>(null) }
    var selectedBrandForAdmin by remember { mutableStateOf<Brand?>(null) }
    var selectedBrandForRedeem by remember { mutableStateOf<Brand?>(null) }
    var showAddBrand by remember { mutableStateOf(false) }
    var showAdmin by remember { mutableStateOf(false) }
    var showRedeem by remember { mutableStateOf(false) }

    val userLoc = rememberUserLocation()
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val notified = remember { mutableStateMapOf<String, Boolean>() }

    // derived data
    val all: List<Brand> = brands
    val featured = all.sortedByDescending { it.priority }.take(3)
    val categoryFiltered = if (selected == "All") all else all.filter { it.category == selected }
    val shown = if (nearYouOnly && userLoc != null) {
        val (uLat, uLng) = userLoc
        categoryFiltered.filter { b ->
            val hasGeo = b.lat != null && b.lng != null
            hasGeo && haversineMeters(uLat, uLng, b.lat!!, b.lng!!) <= b.notifyRadiusMeters
        }
    } else categoryFiltered

    // local notification demo when user enters a brand radius
    LaunchedEffect(userLoc) {
        val loc = userLoc ?: return@LaunchedEffect
        val (uLat, uLng) = loc
        all.forEach { b ->
            if (b.notifyEnabled && b.lat != null && b.lng != null) {
                val d = haversineMeters(uLat, uLng, b.lat!!, b.lng!!)
                if (d <= b.notifyRadiusMeters && notified[b.id] != true) {
                    com.urbansetu.app.util.Notifier.notifyBrand(
                        ctx, id = b.id, title = "${b.name}: ${b.headline}", text = b.notifyMessage
                    )
                    notified[b.id] = true
                }
            }
        }
    }

    // ask notification permission on T+
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* ignore result for demo */ }

    LaunchedEffect(Unit) {
        val needs = android.os.Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                    ctx, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
        if (needs) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    // shared click lambda so a tile selects the brand for redeem too
    val onTileClick: (Brand) -> Unit = { b ->
        selectedBrandForRedeem = b
        onBrandClick(b)
    }

    Box(Modifier.fillMaxSize()) {

        // subtle background
        Image(
            painter = painterResource(R.drawable.hero_banner),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.15f)
        )

        // content
        Column(Modifier.fillMaxSize().padding(16.dp)) {

            HeroRotating(banners = featured)
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterRow(categories, selected) { selected = it }
                Spacer(Modifier.width(12.dp))
                FilterChip(
                    selected = nearYouOnly,
                    onClick = { nearYouOnly = !nearYouOnly },
                    label = { Text(if (nearYouOnly) "Near you (ON)" else "Near you") }
                )
            }
            Spacer(Modifier.height(8.dp))

            Text("Featured today", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            val featuredLarge = all.sortedByDescending { it.priority }.take(3)
            LazyRow(contentPadding = PaddingValues(end = 8.dp)) {
                items(featuredLarge) { BrandWideCard(it, onTileClick) }
            }
            Spacer(Modifier.height(12.dp))

            // rest of grid (exclude featured)
            val featuredIds = featuredLarge.map { it.id }.toSet()
            val rest = shown.filterNot { it.id in featuredIds }

            Text("Add brands and give admin access for Brand owners",
                style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))

            OutlinedButton(onClick = { showAddBrand = true }) { Text("Add Brand") }

            // Keep this inside the same Column as the button, but outside lazy lists.
            if (showAddBrand) {
                AddBrandDialog(
                    onCancel = { showAddBrand = false },
                    onCreate = { created ->
                        BrandRepo.addBrand(created)   // updates repo and recomposes UI
                        showAddBrand = false
                    }
                )
            }

            Spacer(Modifier.height(8.dp))
            // Button next to Add Brand / header (enable only when selected)
            Button(
                onClick = { showAdmin = true },
                enabled = selectedBrandForAdmin != null
            ) { Text("Manage Brand") }

// Open admin screen when allowed
            if (showAdmin && selectedBrandForAdmin != null) {
                BrandAdminScreen(
                    brand = selectedBrandForAdmin!!,
                    onBack = { showAdmin = false },
                    onSave = { updated ->
                        // ✅ Ask the repository to update the brand instead of trying to mutate the list
                        BrandRepo.updateBrand(updated)
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Grid
            LazyColumn {
                listItems(rest) { b ->
                    BrandTile(
                        b = b,
                        onClick = onTileClick,
                        onEdit = { updated ->
                            BrandRepo.updateBrand(updated)
                            selectedBrandForAdmin = updated                // preselect for admin view
                        },
                        currentUserId = currentUserId,
                        openOwnerPanel = { selectedBrandForOwner = it },
                        onManage = { selected ->
                            selectedBrandForAdmin = selected
                            showAdmin = true                               // 👈 opens admin screen below
                        }
                    )
                }
            }

            // Action row (admin / redeem)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showAdmin = true },
                    enabled = selectedBrandForAdmin != null
                ) { Text("Manage Brand") }

                FilledTonalButton(
                    onClick = { showRedeem = true },
                    enabled = selectedBrandForRedeem != null
                ) { Text("Redeem Offer") }
            }
        }
    }

    // Radius editor (owner)
    selectedBrandForOwner?.let { b ->
        BrandOwnerRadiusEditor(
            b = b,
            onSave = { updated ->
                BrandRepo.updateBrand(updated)
                selectedBrandForOwner = null
            },
            onClose = { selectedBrandForOwner = null }
        )
    }

    // Admin screen
    if (showAdmin && selectedBrandForAdmin != null) {
        BrandAdminScreen(
            brand = selectedBrandForAdmin!!,
            onBack = { showAdmin = false },
            onSave = { updated ->
                // persist + trigger recomposition
                BrandRepo.updateBrand(updated)
                // keep the selection pointing at the updated brand
                selectedBrandForAdmin = updated
            }
        )
    }


    // Redeem
    if (showRedeem && selectedBrandForRedeem != null) {
        BrandRedeemSheet(
            brand = selectedBrandForRedeem!!,
            wallet = walletViewModel,
            onClose = { showRedeem = false },
            onRedeemed = { code ->
                Toast.makeText(ctx, "Coupon: $code", Toast.LENGTH_LONG).show()
            }
        )
    }
}
