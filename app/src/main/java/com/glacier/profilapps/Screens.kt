package com.glacier.profilapps

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen() {
    val uri = LocalUriHandler.current
    val float by rememberInfiniteTransition(label = "f").animateFloat(
        -6f, 6f, infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "fy"
    )
    val pulse by rememberInfiniteTransition(label = "p").animateFloat(
        0.3f, 1f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pa"
    )
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Appear(0) {
            Box(Modifier.fillMaxWidth().padding(bottom = 56.dp)) {
                Image(
                    painterResource(R.drawable.banner), null,
                    Modifier.fillMaxWidth().aspectRatio(16f / 9f).graphicsLayer { translationY = float }
                        .shadow(20.dp, RoundedCornerShape(24.dp), ambientColor = Accent, spotColor = Accent)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
                Image(
                    painterResource(R.drawable.logo), null,
                    Modifier.align(Alignment.BottomCenter).offset(y = 48.dp).size(96.dp)
                        .shadow(16.dp, CircleShape, ambientColor = Accent, spotColor = Accent)
                        .clip(CircleShape).border(3.dp, Ice.copy(alpha = pulse), CircleShape)
                )
            }
        }
        Appear(1) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(36.dp))
                Text(Cfg.PROFILE_NAME, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text(Cfg.PROFILE_TAGLINE, color = Ice, fontSize = 15.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Appear(2) {
            GlassCard {
                Text("Tentang", color = Accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(Cfg.PROFILE_BIO, color = Color.White.copy(alpha = .9f), fontSize = 14.sp, lineHeight = 21.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Appear(3) {
            GlassCard {
                Text("Saluran Admin", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Text("Info, update, dan pengumuman terbaru dari admin.", color = Ice.copy(alpha = .75f), fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                GlowButton("Buka Saluran Admin") { uri.openUri(Cfg.ADMIN_CHANNEL) }
            }
        }
        Spacer(Modifier.height(14.dp))
        Appear(4) {
            GlassCard {
                Text("Thanks Support", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Text("Terima kasih buat semua yang sudah support!", color = Ice.copy(alpha = .75f), fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                GlowButton("Buka Thanks Support") { uri.openUri(Cfg.SUPPORT_CHANNEL) }
            }
        }
        Spacer(Modifier.height(20.dp))
        Appear(5) { Text("CONTACT DEV", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth()) }
        Spacer(Modifier.height(10.dp))
        Cfg.CONTACTS.forEachIndexed { i, (label, num) ->
            Appear(6 + i) {
                Column {
                    GlassCard {
                        Text("${i + 1}. $label", color = Accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(pretty(num), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        GlowButton("Chat WhatsApp") { uri.openUri("https://wa.me/$num") }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun pretty(n: String) = "+${n.take(2)} ${n.drop(2).chunked(4).joinToString("-")}"
