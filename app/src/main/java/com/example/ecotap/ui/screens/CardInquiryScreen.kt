package com.example.ecotap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecotap.data.model.RedemptionTransaction
import com.example.ecotap.data.model.User
import com.example.ecotap.data.model.WasteDepositTransaction
import com.example.ecotap.ui.theme.EcoAmberAccent
import com.example.ecotap.ui.theme.EcoGreenContainer
import com.example.ecotap.ui.theme.EcoGreenDark
import com.example.ecotap.ui.theme.EcoGreenPrimary

@Composable
fun CardInquiryScreen(
    user: User,
    deposits: List<WasteDepositTransaction>,
    redemptions: List<RedemptionTransaction>,
    onUpdatePin: (String) -> Unit,
    onBackToMenu: () -> Unit
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf<String?>(null) }

    val userDeposits = deposits.filter { it.userId == user.id }
    val userRedemptions = redemptions.filter { it.userId == user.id }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Virtual NFC Smart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(EcoGreenPrimary, EcoGreenDark, Color(0xFF0D3B11))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌱 ", fontSize = 20.sp)
                                Text("QUEZON CITY ECO-CARD", fontSize = 11.sp, fontWeight = FontWeight.Black, color = EcoAmberAccent, letterSpacing = 1.sp)
                            }

                            Button(
                                onClick = { showPinDialog = !showPinDialog },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EcoAmberAccent),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Change PIN", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Smart Chip Representation
                        Box(
                            modifier = Modifier
                                .size(38.dp, 28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFD54F))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = user.fullName.uppercase(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "UID: ${user.nfcCardId}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = user.barangay,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("ECO-POINTS BALANCE", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                                    Text("${user.ecoPoints} PTS", fontSize = 20.sp, fontWeight = FontWeight.Black, color = EcoAmberAccent)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("TOTAL WASTE RECYCLED", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                                    Text("${user.totalWasteKg} KG", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // PIN Code Change Form Section (Expandable)
            if (showPinDialog) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EcoGreenContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("UPDATE 6-DIGIT PIN CODE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EcoGreenPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newPinInput,
                                onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) newPinInput = it },
                                label = { Text("New 6-Digit PIN") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    if (newPinInput.length == 6) {
                                        onUpdatePin(newPinInput)
                                        pinMessage = "PIN Updated Successfully!"
                                        showPinDialog = false
                                        newPinInput = ""
                                    } else {
                                        pinMessage = "PIN must be 6 digits."
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                            ) {
                                Text("Save PIN", fontWeight = FontWeight.Bold)
                            }
                        }
                        if (pinMessage != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(pinMessage!!, fontSize = 12.sp, color = EcoGreenPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transaction Activity Log
            Text("ACTIVITY TRANSACTION LEDGER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenPrimary)
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(userDeposits) { deposit ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(EcoGreenContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("♻️", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(deposit.categoryName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${deposit.weightKg} kg | ${deposit.formattedDate()}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            Text("+${deposit.pointsEarned} PTS", fontWeight = FontWeight.Black, color = EcoGreenPrimary, fontSize = 15.sp)
                        }
                    }
                }

                items(userRedemptions) { red ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFECB3)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🛍️", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(red.itemTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Redeemed | ${red.formattedDate()}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            Text("-${red.pointsDeducted} PTS", fontWeight = FontWeight.Black, color = Color(0xFFD32F2F), fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Back button
            OutlinedButton(
                onClick = onBackToMenu,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("← Back to Main Service Menu", fontSize = 14.sp, color = EcoGreenPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
