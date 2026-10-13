package com.example.ecotap.ui.theme

import androidx.compose.ui.graphics.Color

// Ultra-Minimalist Palette
val BrandDarkPine   = Color(0xFF1E3A38)  // Primary Deep Green (#1e3a38)
val BrandSageGreen  = Color(0xFF4A7059)  // Accent Soft Green (#4a7059)
val BrandMutedTeal  = Color(0xFF527D6B)  // Muted Teal (#527d6b)
val BrandGoldAmber  = Color(0xFFE5A038)  // Minimal Gold (#e5a038)
val BrandEarthGold  = Color(0xFFB8A26A)  // Earth Accent
val BrandWarmGray   = Color(0xFFE2DFD7)  // Soft Divider Border (#e2dfd7)
val BrandDeepNavy   = Color(0xFF1F2B3E)  // Primary Text Color (#1f2b3e)
val BrandSkyBlue    = Color(0xFFE8F4F8)  // Light Surface Accent
val BrandIceMint    = Color(0xFFF2FAF7)  // Minimal Light Card Surface

// Design System Semantic Mapping
val EcoEmeraldDark = BrandDarkPine
val EcoEmeraldPrimary = BrandDarkPine
val EcoEmeraldLight = BrandSageGreen
val EcoElectricMint = BrandIceMint
val EcoMintContainer = BrandIceMint

val EcoTealPrimary = BrandMutedTeal
val EcoTealContainer = BrandSkyBlue

val EcoAmberGold = BrandGoldAmber
val EcoAmberContainer = BrandEarthGold.copy(alpha = 0.2f)

val EcoSurfaceBg = Color(0xFFFAFCFA)
val EcoCardBg = Color(0xFFFFFFFF)
val EcoCardBorder = BrandWarmGray

val EcoTextDark = BrandDeepNavy
val EcoTextMuted = Color(0xFF6B7A75)
val EcoErrorRed = Color(0xFFD32F2F)

// Backward compatibility color aliases
val EcoGreenPrimary = BrandDarkPine
val EcoGreenDark = BrandDarkPine
val EcoGreenContainer = BrandIceMint
val EcoTealSecondary = BrandMutedTeal
val EcoAmberAccent = BrandGoldAmber
