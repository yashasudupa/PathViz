package com.urbansetu.app.wallet

import android.content.Intent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Column

@Composable
fun CouponDialog(
    coupon: Coupon?,
    onMarkUsed: (String)->Unit,
    onClose: ()->Unit
) {
    if (coupon == null) return
    val ctx = LocalContext.current
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Your coupon") },
        text = {
            Column {
                Text("Code: ${coupon.code}", style = MaterialTheme.typography.titleMedium)
                Text(coupon.title)
                Text(coupon.terms)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "My UrbanSetu coupon: ${coupon.code}")
                }
                ctx.startActivity(Intent.createChooser(share, "Share coupon"))
            }) { Text("Share") }
        },
        dismissButton = {
            TextButton(onClick = {
                onMarkUsed(coupon.id)
                onClose()
            }) { Text("Mark used") }
        }
    )
}