package com.example.ecotap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.ecotap.data.model.AppMode
import com.example.ecotap.nfc.NfcManager
import com.example.ecotap.ui.components.BannerNotification
import com.example.ecotap.ui.components.KioskHeader
import com.example.ecotap.ui.screens.CardInquiryScreen
import com.example.ecotap.ui.screens.KioskMenuScreen
import com.example.ecotap.ui.screens.NfcScanScreen
import com.example.ecotap.ui.screens.PinEntryScreen
import com.example.ecotap.ui.screens.RegisterResidentScreen
import com.example.ecotap.ui.screens.RewardsPosScreen
import com.example.ecotap.ui.theme.EcotapTheme
import com.example.ecotap.ui.viewmodel.EcoTapViewModel

class MainActivity : ComponentActivity() {

    private lateinit var nfcManager: NfcManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        nfcManager = NfcManager(this)

        setContent {
            EcotapTheme {
                val viewModel = remember { EcoTapViewModel() }

                // Setup NFC Hardware Listener
                DisposableEffect(Unit) {
                    nfcManager.setOnTagDiscoveredListener { tagId ->
                        viewModel.onNfcCardTapped(tagId)
                    }
                    onDispose { }
                }

                EcoTapAppContent(
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::nfcManager.isInitialized) {
            nfcManager.enableReaderMode()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::nfcManager.isInitialized) {
            nfcManager.disableReaderMode()
        }
    }
}

@Composable
fun EcoTapAppContent(
    viewModel: EcoTapViewModel
) {
    val currentMode by viewModel.currentMode.collectAsState()
    val authUser by viewModel.authenticatedUser.collectAsState()
    val pinInput by viewModel.pinInput.collectAsState()
    val pinError by viewModel.pinErrorMessage.collectAsState()
    val bannerMsg by viewModel.bannerMessage.collectAsState()
    val unregisteredNfcId by viewModel.unregisteredScannedNfcId.collectAsState()

    val usersList by viewModel.usersList.collectAsState()
    val depositsList by viewModel.depositsList.collectAsState()
    val redemptionsList by viewModel.redemptionsList.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            KioskHeader(
                currentMode = currentMode,
                user = authUser,
                onLogout = { viewModel.logoutSession() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            BannerNotification(
                message = bannerMsg,
                onDismiss = { viewModel.clearBanner() }
            )

            when (currentMode) {
                AppMode.NFC_SCAN -> {
                    NfcScanScreen(
                        users = usersList,
                        onNfcCardTapped = { nfcCardId -> viewModel.onNfcCardTapped(nfcCardId) }
                    )
                }

                AppMode.REGISTRATION -> {
                    RegisterResidentScreen(
                        scannedNfcCardId = unregisteredNfcId ?: "ECO-TAP-NFC",
                        onCreateAccount = { name, address, pin ->
                            viewModel.createAccountAndLinkCard(name, address, pin)
                        },
                        onCancel = { viewModel.navigateTo(AppMode.NFC_SCAN) }
                    )
                }

                AppMode.PIN_ENTRY -> {
                    val user = authUser
                    if (user != null) {
                        PinEntryScreen(
                            user = user,
                            pinInput = pinInput,
                            errorMessage = pinError,
                            onDigitEntered = { digit -> viewModel.onPinDigitEntered(digit) },
                            onBackspace = { viewModel.onPinBackspace() },
                            onClear = { viewModel.onPinClear() },
                            onCancel = { viewModel.logoutSession() }
                        )
                    } else {
                        viewModel.navigateTo(AppMode.NFC_SCAN)
                    }
                }

                AppMode.MAIN_MENU -> {
                    val user = authUser
                    if (user != null) {
                        KioskMenuScreen(
                            user = user,
                            onNavigate = { mode -> viewModel.navigateTo(mode) }
                        )
                    } else {
                        viewModel.navigateTo(AppMode.NFC_SCAN)
                    }
                }

                AppMode.CARD_INQUIRY -> {
                    val user = authUser
                    if (user != null) {
                        CardInquiryScreen(
                            user = user,
                            deposits = depositsList,
                            redemptions = redemptionsList,
                            onUpdatePin = { newPin ->
                                viewModel.repository.updateUserPin(user.id, newPin)
                            },
                            onBackToMenu = { viewModel.navigateTo(AppMode.MAIN_MENU) }
                        )
                    } else {
                        viewModel.navigateTo(AppMode.NFC_SCAN)
                    }
                }

                AppMode.REWARDS_POS -> {
                    val user = authUser
                    if (user != null) {
                        RewardsPosScreen(
                            user = user,
                            rewardItems = viewModel.repository.rewardItems,
                            onRedeemItem = { item -> viewModel.redeemRewardItem(item) },
                            onBackToMenu = { viewModel.navigateTo(AppMode.MAIN_MENU) }
                        )
                    } else {
                        viewModel.navigateTo(AppMode.NFC_SCAN)
                    }
                }

                else -> {
                    viewModel.navigateTo(AppMode.NFC_SCAN)
                }
            }
        }
    }
}
