package com.urbansetu.app.analytics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class BrandMetrics(
    val brandId: String,
    val impressions: Int = 0,
    val clicks: Int = 0,
    val redemptions: Int = 0
)

object AnalyticsRepo {
    private val _all = MutableStateFlow<Map<String, BrandMetrics>>(emptyMap())
    val all: StateFlow<Map<String, BrandMetrics>> = _all

    private val seenImpressions = mutableSetOf<String>()

    fun trackImpression(brandId: String) = update(brandId) { it.copy(impressions = it.impressions + 1) }
    fun trackImpressionOnce(brandId: String) {
        if (seenImpressions.add(brandId)) trackImpression(brandId)
    }
    fun trackClick(brandId: String)      = update(brandId) { it.copy(clicks = it.clicks + 1) }
    fun trackRedemption(brandId: String) = update(brandId) { it.copy(redemptions = it.redemptions + 1) }

    private fun update(id: String, f: (BrandMetrics) -> BrandMetrics) {
        val cur = _all.value.toMutableMap()
        val base = cur[id] ?: BrandMetrics(brandId = id)
        cur[id] = f(base)
        _all.value = cur
    }
}
