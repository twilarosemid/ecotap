package com.example.ecotap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecotap.data.model.User
import com.example.ecotap.ui.theme.BrandDarkPine
import com.example.ecotap.ui.theme.BrandGoldAmber
import com.example.ecotap.ui.theme.BrandIceMint
import com.example.ecotap.ui.theme.EcoErrorRed

@Composable
fun PinEntryScreen(
    user: User,
    pinInput: String,
    errorMessage: String?,
    onDigitEntered: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // User Greeting Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrandDarkPine),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(BrandGoldAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🪪", fontSize = 26.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "NFC CARD VERIFIED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandGoldAmber,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = user.fullName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "UID: ${user.nfcCardId} | ${user.barangay}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // PIN Display Circles
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ENTER 6-DIGIT SECURITY PIN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDarkPine,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 6) {
                        val isFilled = i < pinInput.length
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) BrandDarkPine else Color.Transparent)
                                .border(
                                    2.dp,
                                    if (errorMessage != null) EcoErrorRed else BrandDarkPine,
                                    CircleShape
                                )
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        color = EcoErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Touch Screen PIN Pad
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val padRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "X")
                )

                for (row in padRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (key in row) {
                            val isActionKey = key == "C" || key == "X"
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clickable {
                                        when (key) {
                                            "C" -> onClear()
                                            "X", "⌫" -> onBackspace()
                                            else -> onDigitEntered(key)
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when (key) {
                                        "C" -> Color.Transparent
                                        "X" -> Color.Transparent
                                        else -> BrandIceMint
                                    }
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isActionKey) 0.dp else 1.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = if (isActionKey) 20.sp else 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (key) {
                                            "C" -> BrandDarkPine.copy(alpha = 0.7f)
                                            "X" -> BrandDarkPine.copy(alpha = 0.7f)
                                            else -> BrandDarkPine
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Borderless Flat Cancel Button
            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = BrandDarkPine)
            ) {
                Text("Cancel & Eject NFC Card", color = BrandDarkPine, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
