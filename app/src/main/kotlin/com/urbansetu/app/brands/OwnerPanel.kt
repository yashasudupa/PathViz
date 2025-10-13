package com.urbansetu.app.brands

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.urbansetu.app.util.Notifier
import androidx.compose.ui.platform.LocalContext
import java.util.UUID

@Composable
fun OwnerPanelSheet(
    brand: Brand,
    onClose: () -> Unit
) {
    val ctx = LocalContext.current
    val items by BrandRepo.items.collectAsState()
    val mine = remember(items, brand.id) { BrandRepo.itemsFor(brand.id) }
    val nearby = remember(brand) { BrandRepo.nearbyUsers(brand) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("${brand.name} — Owner Panel") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text("Nearby members in radius: ${nearby.size}", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                if (nearby.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                        items(nearby) { u -> Text("• ${u.name}") }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Items / Offers", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))

                val allItems by BrandRepo.items.collectAsState()

                val mine: List<BrandItem> = remember(brand.id, allItems) {
                    allItems.filter { it.brandId == brand.id }
                }

                if (mine.isEmpty()) {
                    Text("No items yet.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        mine.forEach { item ->
                            Text(
                                "• ${item.title}" +
                                        (item.discountText?.let { " ($it)" } ?: ""),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                var newTitle by remember { mutableStateOf("") }
                var newOffer by remember { mutableStateOf("") }
                OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("New item title") })
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = newOffer, onValueChange = { newOffer = it }, label = { Text("Offer text") })
                Spacer(Modifier.height(6.dp))
                Row {
                    OutlinedButton(onClick = {
                        if (newTitle.isNotBlank()) {
                            BrandRepo.addItem(
                                BrandItem(UUID.randomUUID().toString(), brand.id, title = newTitle, discountText = newOffer)
                            )
                            newTitle = ""; newOffer = ""
                        }
                    }) { Text("Add item") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        // Push a broadcast to users in radius
                        Notifier.notifyBrand(
                            ctx,
                            id = brand.id,
                            title = "${brand.name}: ${brand.headline}",
                            text = brand.notifyMessage
                        )
                    }) { Text("Push notification") }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Close") } }
    )
}