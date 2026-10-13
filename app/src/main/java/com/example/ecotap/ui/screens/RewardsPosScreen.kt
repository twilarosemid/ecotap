package com.example.ecotap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecotap.data.model.RewardItem
import com.example.ecotap.data.model.User
import com.example.ecotap.ui.theme.BrandDarkPine
import com.example.ecotap.ui.theme.BrandEarthGold
import com.example.ecotap.ui.theme.BrandGoldAmber
import com.example.ecotap.ui.theme.BrandIceMint
import com.example.ecotap.ui.theme.BrandWarmGray

@Composable
fun RewardsPosScreen(
    user: User,
    rewardItems: List<RewardItem>,
    onRedeemItem: (RewardItem) -> Unit,
    onBackToMenu: () -> Unit
) {
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
            // Header Banner & Balance
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrandDarkPine),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("REWARDS CATALOG (POS)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandGoldAmber, letterSpacing = 0.5.sp)
                        Text("Supply Goods Catalog", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Resident: ${user.fullName}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("BALANCE", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                            Text("${user.ecoPoints} PTS", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrandGoldAmber)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("AVAILABLE SUPPLY GOODS FOR REDEMPTION:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandDarkPine)
            Spacer(modifier = Modifier.height(6.dp))

            // Reward Items List with Bottom Clearance Padding
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(rewardItems) { item ->
                    val canAfford = user.ecoPoints >= item.pointsCost
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandWarmGray),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(BrandEarthGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (item.iconName) {
                                        "rice" -> "🌾"
                                        "food" -> "🥫"
                                        "oil" -> "🍾"
                                        "hygiene" -> "🧴"
                                        "bag" -> "🛍️"
                                        else -> "🎟️"
                                    },
                                    fontSize = 24.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandDarkPine
                                )
                                Text(
                                    text = item.description,
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = BrandIceMint,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = item.category.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandDarkPine,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "${item.pointsCost} PTS",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAfford) BrandDarkPine else Color.Gray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { onRedeemItem(item) },
                                    enabled = canAfford,
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandDarkPine),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(if (canAfford) "Redeem" else "Need Pts", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Back button
            OutlinedButton(
                onClick = onBackToMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandWarmGray)
            ) {
                Text("← Back to Main Service Menu", fontSize = 13.sp, color = BrandDarkPine, fontWeight = FontWeight.Bold)
            }
        }
    }
}
