package com.urbansetu.app.wallet

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

data class Coupon(
    val id: String,
    val code: String,
    val title: String,     // 👈 Add this
    val terms: String
)

class WalletViewModel : ViewModel() {
    private val _points = MutableStateFlow(200) // demo points
    val points: StateFlow<Int> = _points

    fun canRedeem(cost: Int) = _points.value >= cost

    fun redeem(
        brandId: String,
        title: String,
        terms: String,
        cost: Int
    ): Coupon? {
        return if (canRedeem(cost)) {
            _points.value = _points.value - cost
            Coupon(
                id = UUID.randomUUID().toString(),   // ✅ new unique ID
                code = "URBAN-${brandId.uppercase()}-${(1000..9999).random()}",
                title = title,                       // ✅ from the function parameter
                terms = terms                        // ✅ from the function parameter
            )
        } else null
    }
}
