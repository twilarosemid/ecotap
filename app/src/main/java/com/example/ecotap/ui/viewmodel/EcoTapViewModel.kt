package com.example.ecotap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecotap.data.model.AppMode
import com.example.ecotap.data.model.RedemptionTransaction
import com.example.ecotap.data.model.RewardItem
import com.example.ecotap.data.model.User
import com.example.ecotap.data.model.WasteCategory
import com.example.ecotap.data.model.WasteDepositTransaction
import com.example.ecotap.data.repository.EcoTapRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EcoTapViewModel(val repository: EcoTapRepository = EcoTapRepository()) : ViewModel() {

    // Current Mode / Screen
    private val _currentMode = MutableStateFlow(AppMode.NFC_SCAN)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    // Authenticated User & Scanning State
    private val _scannedNfcCardId = MutableStateFlow<String?>(null)
    val scannedNfcCardId: StateFlow<String?> = _scannedNfcCardId.asStateFlow()

    private val _unregisteredScannedNfcId = MutableStateFlow<String?>(null)
    val unregisteredScannedNfcId: StateFlow<String?> = _unregisteredScannedNfcId.asStateFlow()

    private val _authenticatedUser = MutableStateFlow<User?>(null)
    val authenticatedUser: StateFlow<User?> = _authenticatedUser.asStateFlow()

    // PIN Input state
    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput.asStateFlow()

    private val _pinErrorMessage = MutableStateFlow<String?>(null)
    val pinErrorMessage: StateFlow<String?> = _pinErrorMessage.asStateFlow()

    // Waste Deposit Weighing state
    private val _selectedWasteCategory = MutableStateFlow<WasteCategory>(repository.wasteCategories.first())
    val selectedWasteCategory: StateFlow<WasteCategory> = _selectedWasteCategory.asStateFlow()

    private val _simulatedScaleWeightKg = MutableStateFlow(1.5) // default 1.5kg for scale simulation
    val simulatedScaleWeightKg: StateFlow<Double> = _simulatedScaleWeightKg.asStateFlow()

    private val _tareWeightKg = MutableStateFlow(0.0)
    val tareWeightKg: StateFlow<Double> = _tareWeightKg.asStateFlow()

    private val _lastDepositReceipt = MutableStateFlow<WasteDepositTransaction?>(null)
    val lastDepositReceipt: StateFlow<WasteDepositTransaction?> = _lastDepositReceipt.asStateFlow()

    // Notice Message / Toast feedback
    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    // Reactive lists from repository
    val usersList: StateFlow<List<User>> = repository.users
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val depositsList: StateFlow<List<WasteDepositTransaction>> = repository.deposits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val redemptionsList: StateFlow<List<RedemptionTransaction>> = repository.redemptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ACTIONS ---

    // Handle NFC Card Tap (Real physical hardware tap or simulated tap)
    fun onNfcCardTapped(nfcCardId: String) {
        _scannedNfcCardId.value = nfcCardId

        // Check if user is ALREADY logged in with this exact same NFC card
        val currentUser = _authenticatedUser.value
        val activeModes = setOf(AppMode.MAIN_MENU, AppMode.CARD_INQUIRY, AppMode.REWARDS_POS)
        if (currentUser != null && activeModes.contains(_currentMode.value)) {
            val formattedTappedId = if (nfcCardId.startsWith("ECO-TAP-")) nfcCardId.uppercase() else "ECO-TAP-${nfcCardId.uppercase()}"
            if (currentUser.nfcCardId.equals(formattedTappedId, ignoreCase = true)) {
                // User re-tapped their active card while already logged in
                showBanner("Active Session Confirmed: ${currentUser.fullName}")
                return
            }
        }

        val user = repository.getUserByNfcCard(nfcCardId)
        if (user != null) {
            _authenticatedUser.value = user
            _pinInput.value = ""
            _pinErrorMessage.value = null
            _currentMode.value = AppMode.PIN_ENTRY
            showBanner("NFC Card Recognized: ${user.fullName}")
        } else {
            // New / Unregistered NFC card (e.g. banking card) -> open Resident Account Creation Screen!
            _unregisteredScannedNfcId.value = nfcCardId
            _currentMode.value = AppMode.REGISTRATION
            showBanner("New NFC Card Detected ($nfcCardId). Set up your resident account!")
        }
    }

    // Create Account & Link NFC Card
    fun createAccountAndLinkCard(fullName: String, address: String, pinCode: String) {
        val nfcId = _unregisteredScannedNfcId.value ?: "ECO-TAP-" + System.currentTimeMillis().toString().takeLast(6)
        val newResident = repository.registerNewResident(fullName, address, nfcId, pinCode)
        _currentMode.value = AppMode.NFC_SCAN
        showBanner("Account created for ${newResident.fullName}! Tap your card again to log in.")
    }

    // PIN Pad Digit Entry
    fun onPinDigitEntered(digit: String) {
        if (_pinInput.value.length < 6) {
            _pinInput.value += digit
            _pinErrorMessage.value = null
            if (_pinInput.value.length == 6) {
                verifyPinAndProceed()
            }
        }
    }

    fun onPinBackspace() {
        if (_pinInput.value.isNotEmpty()) {
            _pinInput.value = _pinInput.value.dropLast(1)
            _pinErrorMessage.value = null
        }
    }

    fun onPinClear() {
        _pinInput.value = ""
        _pinErrorMessage.value = null
    }

    fun verifyPinAndProceed() {
        val user = _authenticatedUser.value ?: return
        val isVerified = repository.verifyUserPin(user, _pinInput.value)
        if (isVerified) {
            _pinErrorMessage.value = null
            _currentMode.value = AppMode.MAIN_MENU
            showBanner("Welcome, ${user.fullName}!")
        } else {
            _pinErrorMessage.value = "Invalid 6-Digit PIN Code. Please try again."
            _pinInput.value = ""
        }
    }

    // Navigation Controls
    fun navigateTo(mode: AppMode) {
        _currentMode.value = mode
    }

    fun logoutSession() {
        _authenticatedUser.value = null
        _scannedNfcCardId.value = null
        _pinInput.value = ""
        _pinErrorMessage.value = null
        _currentMode.value = AppMode.NFC_SCAN
        showBanner("Session ended. Card ejected.")
    }

    // Scale Controls & Deposit
    fun selectWasteCategory(category: WasteCategory) {
        _selectedWasteCategory.value = category
    }

    fun setSimulatedScaleWeight(weight: Double) {
        _simulatedScaleWeightKg.value = String.format("%.2f", weight.coerceAtLeast(0.1)).toDouble()
    }

    fun zeroScaleTare() {
        _tareWeightKg.value = _simulatedScaleWeightKg.value
        showBanner("Digital Scale Tared to 0.00 kg.")
    }

    fun confirmWasteDeposit() {
        val user = _authenticatedUser.value ?: return
        val category = _selectedWasteCategory.value
        val netWeightKg = (_simulatedScaleWeightKg.value - _tareWeightKg.value).coerceAtLeast(0.05)

        val receipt = repository.processWasteDeposit(user, category, netWeightKg)
        _lastDepositReceipt.value = receipt

        // Refresh authenticated user state with updated balance
        _authenticatedUser.value = repository.users.value.find { it.id == user.id }
        _tareWeightKg.value = 0.0
        _currentMode.value = AppMode.DEPOSIT_RECEIPT
        showBanner("Deposit Recorded! Earned ${receipt.pointsEarned} Eco-Points!")
    }

    // Reward POS Redemption
    fun redeemRewardItem(rewardItem: RewardItem) {
        val user = _authenticatedUser.value ?: return
        val (success, message) = repository.processRedemption(user, rewardItem)
        if (success) {
            _authenticatedUser.value = repository.users.value.find { it.id == user.id }
        }
        showBanner(message)
    }

    private var bannerJob: Job? = null

    // Banner Feedback with 10-second auto-dismiss
    fun showBanner(message: String) {
        _bannerMessage.value = message
        bannerJob?.cancel()
        bannerJob = viewModelScope.launch {
            delay(10000L) // Auto-dismiss after 10 seconds
            if (_bannerMessage.value == message) {
                _bannerMessage.value = null
            }
        }
    }

    fun clearBanner() {
        bannerJob?.cancel()
        _bannerMessage.value = null
    }
}
