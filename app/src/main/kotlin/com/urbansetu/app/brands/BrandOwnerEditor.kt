package com.urbansetu.app.brands

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BrandOwnerRadiusEditor(
    b: Brand,
    onSave: (Brand) -> Unit,
    onClose: () -> Unit
) {
    var enabled by remember { mutableStateOf(b.notifyEnabled) }
    var radius by remember { mutableStateOf(b.notifyRadiusMeters.toFloat()) }
    var message by remember { mutableStateOf(b.notifyMessage) }

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    b.copy(
                        notifyEnabled = enabled,
                        notifyRadiusMeters = radius.toInt(),
                        notifyMessage = message
                    )
                )
                onClose()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancel") } },
        title = { Text("Proximity notification") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable notifications")
                    Spacer(Modifier.weight(1f))
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                Spacer(Modifier.height(12.dp))
                Text("Radius: ${radius.toInt()} m")
                Slider(
                    value = radius,
                    onValueChange = { radius = it },
                    valueRange = 200f..3000f
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Notification message") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
