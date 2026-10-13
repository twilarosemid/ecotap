package com.example.ecotap.data.repository

import android.util.Log
import com.example.ecotap.data.model.CategoryAggregate
import com.example.ecotap.data.model.RedemptionTransaction
import com.example.ecotap.data.model.RewardItem
import com.example.ecotap.data.model.TimeInterval
import com.example.ecotap.data.model.TimeframeStats
import com.example.ecotap.data.model.User
import com.example.ecotap.data.model.WasteCategory
import com.example.ecotap.data.model.WasteDepositTransaction
import com.example.ecotap.data.remote.DepositRequest
import com.example.ecotap.data.remote.MongoApiClient
import com.example.ecotap.data.remote.PinUpdateRequest
import com.example.ecotap.data.remote.RedemptionRequest
import com.example.ecotap.data.remote.UserRegistrationRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class EcoTapRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Pre-defined Solid Waste Categories with Points & CO2 Factors
    val wasteCategories = listOf(
        WasteCategory(
            id = "cat_plastic",
            name = "Plastics (PET & HDPE)",
            pointsPerKg = 15,
            description = "Plastic bottles, food containers, jugs, caps",
            colorHex = 0xFF2E7D32,
            co2FactorKgPerKg = 1.5
        ),
        WasteCategory(
            id = "cat_paper",
            name = "Paper & Cardboard",
            pointsPerKg = 8,
            description = "Cardboard boxes, newsprint, office paper, brochures",
            colorHex = 0xFF1565C0,
            co2FactorKgPerKg = 0.9
        ),
        WasteCategory(
            id = "cat_glass",
            name = "Glass Bottles & Containers",
            pointsPerKg = 10,
            description = "Beverage bottles, condiment jars, glass containers",
            colorHex = 0xFF00838F,
            co2FactorKgPerKg = 0.5
        ),
        WasteCategory(
            id = "cat_metal",
            name = "Metal & Aluminum Cans",
            pointsPerKg = 22,
            description = "Soda cans, tin food cans, scrap metals",
            colorHex = 0xFFD84315,
            co2FactorKgPerKg = 2.8
        ),
        WasteCategory(
            id = "cat_ewaste",
            name = "Electronic Waste (E-Waste)",
            pointsPerKg = 35,
            description = "Old cables, small appliances, circuits, batteries",
            colorHex = 0xFF6A1B9A,
            co2FactorKgPerKg = 3.2
        ),
        WasteCategory(
            id = "cat_organic",
            name = "Organic & Compostables",
            pointsPerKg = 5,
            description = "Fruit peels, garden clippings, biodegradable waste",
            colorHex = 0xFF5D4037,
            co2FactorKgPerKg = 0.4
        )
    )

    // Pre-defined Reward Items for Point-of-Sale (POS) Supply Goods
    val rewardItems = listOf(
        RewardItem(
            id = "rew_rice_5kg",
            title = "Premium Rice (5 kg)",
            description = "High quality Sinandomeng rice pack",
            pointsCost = 250,
            category = "Food Staples",
            iconName = "rice"
        ),
        RewardItem(
            id = "rew_canned_pack",
            title = "Pantry Pack (Canned Goods)",
            description = "3x Sardines, 2x Corned Beef, 2x Tuna",
            pointsCost = 120,
            category = "Food Staples",
            iconName = "food"
        ),
        RewardItem(
            id = "rew_oil_1l",
            title = "Cooking Oil (1 Liter)",
            description = "Pure coconut cooking oil bottle",
            pointsCost = 90,
            category = "Pantry",
            iconName = "oil"
        ),
        RewardItem(
            id = "rew_hygiene_kit",
            title = "Family Hygiene Kit",
            description = "Bath soaps, shampoo sachets, toothpaste, alcohol",
            pointsCost = 110,
            category = "Home Essentials",
            iconName = "hygiene"
        ),
        RewardItem(
            id = "rew_ecobag_set",
            title = "ECO-TAP Heavy Duty Tote Bag Set",
            description = "Re-usable canvas shopping bags (Set of 2)",
            pointsCost = 45,
            category = "Home Essentials",
            iconName = "bag"
        ),
        RewardItem(
            id = "rew_grocery_voucher",
            title = "Barangay Mart Voucher (₱200)",
            description = "Redeemable at participating Quezon City community stores",
            pointsCost = 200,
            category = "Vouchers",
            iconName = "voucher"
        )
    )

    // Initial Sample Residents (Fallback if MongoDB offline)
    private val initialUsers = mutableListOf(
        User(
            id = "usr_001",
            nfcCardId = "ECO-TAP-8A19B",
            pinCode = "123456",
            fullName = "Fraela Mae Aguinaldo",
            address = "Blk 12 Lot 4, Elliptical Rd, Diliman",
            barangay = "Barangay Central, Quezon City",
            ecoPoints = 380,
            totalWasteKg = 28.5
        ),
        User(
            id = "usr_002",
            nfcCardId = "ECO-TAP-3F72C",
            pinCode = "654321",
            fullName = "James Ryan Balanon",
            address = "105 Mayaman St, UP Village",
            barangay = "Barangay Central, Quezon City",
            ecoPoints = 510,
            totalWasteKg = 42.0
        ),
        User(
            id = "usr_003",
            nfcCardId = "ECO-TAP-9C44E",
            pinCode = "112233",
            fullName = "Rochelyn Cabatlao",
            address = "78 Kalayaan Ave, Quezon City",
            barangay = "Barangay Central, Quezon City",
            ecoPoints = 195,
            totalWasteKg = 15.2
        ),
        User(
            id = "usr_004",
            nfcCardId = "ECO-TAP-7B88A",
            pinCode = "888888",
            fullName = "Remuel Fernan",
            address = "45 Matalino St, Diliman",
            barangay = "Barangay Central, Quezon City",
            ecoPoints = 640,
            totalWasteKg = 55.8
        )
    )

    private val initialDeposits = mutableListOf<WasteDepositTransaction>()
    private val initialRedemptions = mutableListOf<RedemptionTransaction>()

    // Reactive State Flows
    private val _users = MutableStateFlow<List<User>>(initialUsers)
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _deposits = MutableStateFlow<List<WasteDepositTransaction>>(initialDeposits)
    val deposits: StateFlow<List<WasteDepositTransaction>> = _deposits.asStateFlow()

    private val _redemptions = MutableStateFlow<List<RedemptionTransaction>>(initialRedemptions)
    val redemptions: StateFlow<List<RedemptionTransaction>> = _redemptions.asStateFlow()

    init {
        // Sync data with MongoDB Atlas on Startup
        syncFromMongoDBAtlas()
    }

    fun syncFromMongoDBAtlas() {
        repositoryScope.launch {
            try {
                val usersResponse = MongoApiClient.apiService.getUsers()
                if (usersResponse.isSuccessful && !usersResponse.body().isNullOrEmpty()) {
                    _users.value = usersResponse.body()!!
                    Log.d("EcoTapRepo", "Synced ${usersResponse.body()!!.size} users from MongoDB Atlas!")
                }

                val depositsResponse = MongoApiClient.apiService.getDeposits()
                if (depositsResponse.isSuccessful && depositsResponse.body() != null) {
                    _deposits.value = depositsResponse.body()!!
                    Log.d("EcoTapRepo", "Synced ${depositsResponse.body()!!.size} deposits from MongoDB Atlas!")
                }

                val redemptionsResponse = MongoApiClient.apiService.getRedemptions()
                if (redemptionsResponse.isSuccessful && redemptionsResponse.body() != null) {
                    _redemptions.value = redemptionsResponse.body()!!
                    Log.d("EcoTapRepo", "Synced ${redemptionsResponse.body()!!.size} redemptions from MongoDB Atlas!")
                }
            } catch (e: Exception) {
                Log.e("EcoTapRepo", "MongoDB Atlas sync notice: ${e.message} (Using local cache)")
            }
        }
    }

    // Find User by NFC Card UID
    fun getUserByNfcCard(nfcCardId: String): User? {
        val trimmed = nfcCardId.trim().uppercase()
        return _users.value.find { it.nfcCardId.uppercase() == trimmed }
    }

    // Verify User PIN
    fun verifyUserPin(user: User, pinInput: String): Boolean {
        return user.pinCode == pinInput
    }

    // Process Waste Deposit
    fun processWasteDeposit(
        user: User,
        category: WasteCategory,
        weightKg: Double
    ): WasteDepositTransaction {
        val pointsEarned = (weightKg * category.pointsPerKg).toInt()
        val depositTx = WasteDepositTransaction(
            id = "tx_dep_" + UUID.randomUUID().toString().take(8),
            userId = user.id,
            userName = user.fullName,
            nfcCardId = user.nfcCardId,
            categoryId = category.id,
            categoryName = category.name,
            weightKg = weightKg,
            pointsEarned = pointsEarned,
            timestamp = System.currentTimeMillis()
        )

        // Local state update
        val updatedUsers = _users.value.map { u ->
            if (u.id == user.id) {
                u.copy(
                    ecoPoints = u.ecoPoints + pointsEarned,
                    totalWasteKg = u.totalWasteKg + weightKg
                )
            } else u
        }
        _users.value = updatedUsers
        _deposits.value = listOf(depositTx) + _deposits.value

        // Asynchronously persist to MongoDB Atlas
        repositoryScope.launch {
            try {
                MongoApiClient.apiService.recordDeposit(
                    DepositRequest(
                        userId = user.id,
                        userName = user.fullName,
                        nfcCardId = user.nfcCardId,
                        categoryId = category.id,
                        categoryName = category.name,
                        weightKg = weightKg,
                        pointsEarned = pointsEarned
                    )
                )
                Log.d("EcoTapRepo", "Persisted Deposit to MongoDB Atlas!")
            } catch (e: Exception) {
                Log.e("EcoTapRepo", "Failed to persist deposit to MongoDB: ${e.message}")
            }
        }

        return depositTx
    }

    // Process Reward Redemption
    fun processRedemption(
        user: User,
        rewardItem: RewardItem
    ): Pair<Boolean, String> {
        if (user.ecoPoints < rewardItem.pointsCost) {
            return Pair(false, "Insufficient Eco-Points. Required: ${rewardItem.pointsCost}, Current: ${user.ecoPoints}")
        }

        val redemptionTx = RedemptionTransaction(
            id = "tx_red_" + UUID.randomUUID().toString().take(8),
            userId = user.id,
            userName = user.fullName,
            nfcCardId = user.nfcCardId,
            itemId = rewardItem.id,
            itemTitle = rewardItem.title,
            pointsDeducted = rewardItem.pointsCost,
            timestamp = System.currentTimeMillis()
        )

        // Local state update
        val updatedUsers = _users.value.map { u ->
            if (u.id == user.id) {
                u.copy(ecoPoints = u.ecoPoints - rewardItem.pointsCost)
            } else u
        }
        _users.value = updatedUsers
        _redemptions.value = listOf(redemptionTx) + _redemptions.value

        // Asynchronously persist to MongoDB Atlas
        repositoryScope.launch {
            try {
                MongoApiClient.apiService.recordRedemption(
                    RedemptionRequest(
                        userId = user.id,
                        userName = user.fullName,
                        nfcCardId = user.nfcCardId,
                        itemId = rewardItem.id,
                        itemTitle = rewardItem.title,
                        pointsDeducted = rewardItem.pointsCost
                    )
                )
                Log.d("EcoTapRepo", "Persisted Redemption to MongoDB Atlas!")
            } catch (e: Exception) {
                Log.e("EcoTapRepo", "Failed to persist redemption to MongoDB: ${e.message}")
            }
        }

        return Pair(true, "Redemption successful! ${rewardItem.pointsCost} points deducted.")
    }

    // Update Resident PIN
    fun updateUserPin(userId: String, newPin: String): Boolean {
        if (newPin.length != 6 || !newPin.all { it.isDigit() }) return false
        _users.value = _users.value.map { u ->
            if (u.id == userId) u.copy(pinCode = newPin) else u
        }

        // Asynchronously persist PIN change to MongoDB Atlas
        repositoryScope.launch {
            try {
                MongoApiClient.apiService.updateUserPin(userId, PinUpdateRequest(newPin))
                Log.d("EcoTapRepo", "Updated PIN in MongoDB Atlas!")
            } catch (e: Exception) {
                Log.e("EcoTapRepo", "Failed to update PIN in MongoDB: ${e.message}")
            }
        }

        return true
    }

    // Register New Resident
    fun registerNewResident(fullName: String, address: String, nfcCardId: String, pinCode: String): User {
        val formattedNfcId = if (nfcCardId.startsWith("ECO-TAP-")) nfcCardId.uppercase() else "ECO-TAP-${nfcCardId.uppercase()}"
        val newUser = User(
            id = "usr_" + UUID.randomUUID().toString().take(6),
            nfcCardId = formattedNfcId,
            pinCode = pinCode.ifEmpty { "123456" },
            fullName = fullName,
            address = address,
            barangay = "Barangay Central, Quezon City",
            ecoPoints = 50,
            totalWasteKg = 0.0
        )
        _users.value = _users.value + newUser

        // Asynchronously persist new resident to MongoDB Atlas
        repositoryScope.launch {
            try {
                val res = MongoApiClient.apiService.registerUser(
                    UserRegistrationRequest(
                        fullName = fullName,
                        address = address,
                        nfcCardId = formattedNfcId,
                        pinCode = pinCode
                    )
                )
                if (res.isSuccessful) {
                    Log.d("EcoTapRepo", "Successfully persisted new resident (${newUser.fullName}) to MongoDB Atlas!")
                } else {
                    Log.e("EcoTapRepo", "MongoDB Atlas registration error: ${res.code()}")
                }
            } catch (e: Exception) {
                Log.e("EcoTapRepo", "Failed to register resident in MongoDB: ${e.message}")
            }
        }

        return newUser
    }

    // Calculate Analytics per Timeframe
    fun getAnalytics(interval: TimeInterval): TimeframeStats {
        val nowMs = System.currentTimeMillis()
        val limitMs = when (interval) {
            TimeInterval.DAILY -> 24 * 60 * 60 * 1000L
            TimeInterval.WEEKLY -> 7 * 24 * 60 * 60 * 1000L
            TimeInterval.MONTHLY -> 30 * 24 * 60 * 60 * 1000L
        }

        val filteredDeposits = _deposits.value.filter { (nowMs - it.timestamp) <= limitMs }

        val totalWeight = filteredDeposits.sumOf { it.weightKg }
        val totalTx = filteredDeposits.size
        val totalPoints = filteredDeposits.sumOf { it.pointsEarned }

        var totalCo2 = 0.0
        val aggregates = wasteCategories.map { category ->
            val catDeposits = filteredDeposits.filter { it.categoryId == category.id }
            val catWeight = catDeposits.sumOf { it.weightKg }
            val catPoints = catDeposits.sumOf { it.pointsEarned }
            totalCo2 += catWeight * category.co2FactorKgPerKg

            val pct = if (totalWeight > 0) (catWeight / totalWeight) * 100.0 else 0.0
            CategoryAggregate(
                categoryName = category.name,
                totalWeightKg = catWeight,
                totalPoints = catPoints,
                percentage = pct
            )
        }

        return TimeframeStats(
            interval = interval,
            totalWeightKg = totalWeight,
            totalTransactions = totalTx,
            totalPointsIssued = totalPoints,
            co2SavedKg = totalCo2,
            categoryAggregates = aggregates
        )
    }
}
