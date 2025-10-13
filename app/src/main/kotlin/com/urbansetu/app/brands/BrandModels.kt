package com.urbansetu.app.brands

data class Brand(
    val id: String,
    val name: String,
    val logoRes: Int,
    val category: String,
    val headline: String,
    val subtext: String,
    val validity: String,
    val isNew: Boolean = false,
    val priority: Int = 50,
    val fakeDistanceMeters: Int? = null,

    // ✅ owner + geo + messaging
    val ownerId: String = "owner_1",                 // who can manage this brand
    val lat: Double? = null,
    val lng: Double? = null,
    val notifyEnabled: Boolean = false,
    val notifyRadiusMeters: Int = 800,
    val notifyMessage: String = "New offer near you!",

    // ✅ optional brand items (offers)
    val items: List<BrandItem> = emptyList()
)