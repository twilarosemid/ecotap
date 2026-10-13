package com.example.ecotap.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppMode {
    NFC_SCAN,
    REGISTRATION,
    PIN_ENTRY,
    MAIN_MENU,
    CARD_INQUIRY,
    WASTE_WEIGHING,
    DEPOSIT_RECEIPT,
    REWARDS_POS,
    ADMIN_DASHBOARD,
    ADMIN_USER_MGMT
}

enum class TimeInterval {
    DAILY,
    WEEKLY,
    MONTHLY
}

data class User(
    val id: String,
    val nfcCardId: String,
    val pinCode: String,
    val fullName: String,
    val address: String,
    val barangay: String = "Barangay Central, Quezon City",
    val ecoPoints: Int,
    val totalWasteKg: Double,
    val registeredDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
)

data class WasteCategory(
    val id: String,
    val name: String,
    val pointsPerKg: Int,
    val description: String,
    val colorHex: Long,
    val co2FactorKgPerKg: Double // kg CO2 saved per kg waste recycled
)

data class WasteDepositTransaction(
    val id: String,
    val userId: String,
    val userName: String,
    val nfcCardId: String,
    val categoryId: String,
    val categoryName: String,
    val weightKg: Double,
    val pointsEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun formattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

data class RewardItem(
    val id: String,
    val title: String,
    val description: String,
    val pointsCost: Int,
    val category: String, // e.g., "Food Staples", "Pantry", "Home Essentials", "Vouchers"
    val iconName: String
)

data class RedemptionTransaction(
    val id: String,
    val userId: String,
    val userName: String,
    val nfcCardId: String,
    val itemId: String,
    val itemTitle: String,
    val pointsDeducted: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun formattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

data class CategoryAggregate(
    val categoryName: String,
    val totalWeightKg: Double,
    val totalPoints: Int,
    val percentage: Double
)

data class TimeframeStats(
    val interval: TimeInterval,
    val totalWeightKg: Double,
    val totalTransactions: Int,
    val totalPointsIssued: Int,
    val co2SavedKg: Double,
    val categoryAggregates: List<CategoryAggregate>
)
