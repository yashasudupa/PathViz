package com.urbansetu.app.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

@Composable
fun AnalyticsSummary() {
    val all by AnalyticsRepo.all.collectAsState()
    val totals = all.values.fold(Triple(0, 0, 0)) { acc, m ->
        Triple(acc.first + m.impressions, acc.second + m.clicks, acc.third + m.redemptions)
    }

    Text(
        "Total: Views ${totals.first} · Clicks ${totals.second} · Redeems ${totals.third}",
        style = MaterialTheme.typography.labelMedium
    )
}


