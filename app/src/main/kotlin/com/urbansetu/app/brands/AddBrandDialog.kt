package com.urbansetu.app.brands

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddBrandDialog(
    onCancel: () -> Unit,
    onCreate: (Brand) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Cafés") }
    var headline by remember { mutableStateOf("10% OFF") }
    var subtext by remember { mutableStateOf("On ₹499+") }
    var validity by remember { mutableStateOf("Today only") }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }

    // for demo: make current user the owner so “Manage” shows
    val ownerId = "owner_1"

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Add a brand") },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") })
                OutlinedTextField(category, { category = it }, label = { Text("Category") })
                OutlinedTextField(headline, { headline = it }, label = { Text("Headline") })
                OutlinedTextField(subtext, { subtext = it }, label = { Text("Subtext") })
                OutlinedTextField(validity, { validity = it }, label = { Text("Validity") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lat, onValueChange = { lat = it },
                        label = { Text("Lat (optional)") }, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lng, onValueChange = { lng = it },
                        label = { Text("Lng (optional)") }, modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    val newBrand = Brand(
                        id = "b-" + System.nanoTime(),
                        name = name,
                        // use any of your existing drawables; can swap to material icon fallback (see §4)
                        logoRes = com.urbansetu.app.R.drawable.ic_grocery,
                        category = category,
                        headline = headline,
                        subtext = subtext,
                        validity = validity,
                        isNew = true,
                        priority = 80,
                        fakeDistanceMeters = 500,
                        ownerId = ownerId,
                        lat = lat.toDoubleOrNull(),
                        lng = lng.toDoubleOrNull(),
                        notifyEnabled = false,
                        notifyRadiusMeters = 800,
                        notifyMessage = "New offer near you!",
                        items = emptyList()
                    )
                    onCreate(newBrand)
                }
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}
