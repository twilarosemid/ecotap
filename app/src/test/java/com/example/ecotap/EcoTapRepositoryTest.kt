package com.example.ecotap

import com.example.ecotap.data.model.TimeInterval
import com.example.ecotap.data.repository.EcoTapRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EcoTapRepositoryTest {

    private lateinit var repository: EcoTapRepository

    @Before
    fun setUp() {
        repository = EcoTapRepository()
    }

    @Test
    fun testFindUserByNfcCard() {
        val user = repository.getUserByNfcCard("ECO-TAP-8A19B")
        assertNotNull(user)
        assertEquals("Fraela Mae Aguinaldo", user?.fullName)
    }

    @Test
    fun testVerifyUserPin() {
        val user = repository.getUserByNfcCard("ECO-TAP-8A19B")!!
        assertTrue(repository.verifyUserPin(user, "123456"))
        assertFalse(repository.verifyUserPin(user, "000000"))
    }

    @Test
    fun testProcessWasteDeposit() {
        val user = repository.getUserByNfcCard("ECO-TAP-8A19B")!!
        val initialPoints = user.ecoPoints
        val initialWaste = user.totalWasteKg

        val plasticCategory = repository.wasteCategories.first { it.id == "cat_plastic" }
        val depositWeight = 4.0 // 4 kg plastic

        val receipt = repository.processWasteDeposit(user, plasticCategory, depositWeight)

        val expectedPointsEarned = (depositWeight * plasticCategory.pointsPerKg).toInt() // 4 * 15 = 60
        assertEquals(expectedPointsEarned, receipt.pointsEarned)

        val updatedUser = repository.getUserByNfcCard("ECO-TAP-8A19B")!!
        assertEquals(initialPoints + expectedPointsEarned, updatedUser.ecoPoints)
        assertEquals(initialWaste + depositWeight, updatedUser.totalWasteKg, 0.01)
    }

    @Test
    fun testProcessRedemption() {
        val user = repository.getUserByNfcCard("ECO-TAP-7B88A")!! // Remuel: 640 pts
        val rewardItem = repository.rewardItems.first { it.id == "rew_rice_5kg" } // 250 pts

        val initialPoints = user.ecoPoints
        val (success, _) = repository.processRedemption(user, rewardItem)

        assertTrue(success)
        val updatedUser = repository.getUserByNfcCard("ECO-TAP-7B88A")!!
        assertEquals(initialPoints - rewardItem.pointsCost, updatedUser.ecoPoints)
    }

    @Test
    fun testAnalyticsGeneration() {
        val dailyStats = repository.getAnalytics(TimeInterval.DAILY)
        assertNotNull(dailyStats)
        assertTrue(dailyStats.totalWeightKg >= 0.0)
        assertEquals(6, dailyStats.categoryAggregates.size)
    }

    @Test
    fun testRegisterNewResident() {
        val newUser = repository.registerNewResident(
            fullName = "Jared Sacyaten",
            address = "Quezon City Hall",
            nfcCardId = "ECO-TAP-ABC12",
            pinCode = "999888"
        )

        assertNotNull(newUser)
        val fetchedUser = repository.getUserByNfcCard("ECO-TAP-ABC12")
        assertNotNull(fetchedUser)
        assertEquals("Jared Sacyaten", fetchedUser?.fullName)
    }
}
