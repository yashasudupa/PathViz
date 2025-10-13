package com.urbansetu.app.brands

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import com.urbansetu.app.analytics.AnalyticsRepo
import com.urbansetu.app.wallet.WalletViewModel

@Composable
fun BrandRedeemSheet(
    brand: Brand,
    wallet: WalletViewModel,
    onClose: () -> Unit,
    onRedeemed: (String) -> Unit // returns coupon code
) {
    val points by wallet.points.collectAsState()
    val cost = 50 // demo cost per coupon

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Redeem at ${brand.name}") },
        text = { /* ... your existing text ... */ },
        confirmButton = {
            val enabled = wallet.canRedeem(cost)
            TextButton(
                enabled = enabled,
                onClick = {
                    val coupon = wallet.redeem(
                        brandId = brand.id,
                        title = brand.headline,
                        terms = "Valid once per user. Not combinable with other offers.",
                        cost = cost
                    )
                    if (coupon != null) {
                        // 🔥 count a redemption ONLY on success
                        AnalyticsRepo.trackRedemption(brand.id)
                        onRedeemed(coupon.code)
                    }
                    onClose()
                }
            ) { Text(if (enabled) "Redeem" else "Not enough points") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Close") } }
    )
}