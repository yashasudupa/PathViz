package com.urbansetu.app.brands

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.*
import com.urbansetu.app.R

private val demoBrands = listOf(
    Brand(
        id = "b1",
        name = "FreshMart",
        logoRes = R.drawable.ic_offer,      // ✅ replace with your drawable
        category = "Supermarkets",
        headline = "₹150 cashback on groceries",
        subtext = "Valid on orders above ₹999",
        validity = "Valid today",
        isNew = true,
        priority = 90,
        lat = 12.9716,
        lng = 77.5946
    ),
    Brand(
        id = "b2",
        name = "CafeBrew",
        logoRes = R.drawable.ic_cafe,       // ✅ replace with real drawable
        category = "Cafés",
        headline = "Buy 1 Get 1 Latte",
        subtext = "Available weekdays 4–6 PM",
        validity = "Valid this week",
        priority = 70,
        lat = 12.9730,
        lng = 77.5960
    )
)


// ---------- Models ----------
data class BrandItem(
    val id: String = "itm-" + System.nanoTime(),
    val brandId: String,              // ✅ needed for itemsFor(brandId)
    val title: String,
    val subtitle: String? = null,
    val price: String? = null,
    val discountText: String? = null
)

data class BrandOwner(val id: String, val name: String, val phone: String)

data class MemberUser(
    val id: String,
    val name: String,
    val lat: Double?,
    val lng: Double?
)

// ---------- Repository (in-memory demo) ----------
object BrandRepo {
    // ✅ Start empty; BrandsScreen will observe this. AddBrandDialog will push into this.
    private val _brands = MutableStateFlow<List<Brand>>(emptyList())
    val brands: StateFlow<List<Brand>> = _brands

    private val _items = MutableStateFlow<List<BrandItem>>(emptyList())
    val items: StateFlow<List<BrandItem>> = _items

    // ✅ Fake users near a city center (so “nearby” demos work)
    private val _users = MutableStateFlow(
        listOf(
            MemberUser("u1", "Aarav", 12.9719, 77.5947),
            MemberUser("u2", "Diya",  12.9752, 77.6001),
            MemberUser("u3", "Rohit", 12.9698, 77.5920),
        )
    )
    val users: StateFlow<List<MemberUser>> = _users

    // ---- Brand CRUD ----
    fun addBrand(b: Brand) {
        _brands.value = _brands.value + b
    }

    fun updateBrand(updated: Brand) {
        _brands.value = _brands.value.map { if (it.id == updated.id) updated else it }
    }

    fun getBrandById(id: String): Brand? = _brands.value.find { it.id == id }

    fun brandsForOwner(ownerId: String) = _brands.value.filter { it.ownerId == ownerId }

    // ---- Items per brand ----
    fun addItem(item: BrandItem) {
        _items.value = _items.value + item
    }

    fun itemsFor(brandId: String): List<BrandItem> =
        _items.value.filter { it.brandId == brandId }

    // ---- Nearby users (by radius) ----
    fun nearbyUsers(brand: Brand): List<MemberUser> {
        val lat = brand.lat ?: return emptyList()
        val lng = brand.lng ?: return emptyList()
        val r = brand.notifyRadiusMeters.toDouble()
        return _users.value.filter { u ->
            u.lat != null && u.lng != null &&
                    haversineMeters(lat, lng, u.lat, u.lng) <= r
        }
    }

    // ---- distance helper ----
    private fun haversineMeters(aLat: Double, aLng: Double, bLat: Double, bLng: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(bLat - aLat)
        val dLng = Math.toRadians(bLng - aLng)
        val la1 = Math.toRadians(aLat); val la2 = Math.toRadians(bLat)
        val x = sin(dLat / 2) * sin(dLat / 2) +
                cos(la1) * cos(la2) * sin(dLng / 2) * sin(dLng / 2)
        return 2 * R * asin(sqrt(x))
    }
    fun resetWithSamples() { _brands.value = demoBrands }
    fun isEmpty(): Boolean = _brands.value.isEmpty()
}
