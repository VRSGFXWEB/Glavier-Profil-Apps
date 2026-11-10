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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen() {
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
                    Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                        .graphicsLayer { translationY = float }
                        .shadow(20.dp, RoundedCornerShape(24.dp), ambientColor = Accent, spotColor = Accent)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
                Image(
                    painterResource(R.drawable.logo), null,
                    Modifier.align(Alignment.BottomCenter).offset(y = 48.dp).size(96.dp)
                        .shadow(16.dp, CircleShape, ambientColor = Accent, spotColor = Accent)
                        .clip(CircleShape)
                        .border(3.dp, Ice.copy(alpha = pulse), CircleShape)
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
                Cfg.PROFILE_INFO.forEachIndexed { i, (k, v) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(k, color = Ice.copy(alpha = .7f), fontSize = 14.sp)
                        Text(v, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (i != Cfg.PROFILE_INFO.lastIndex)
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x22BFE6FF)))
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun LinkScreen(icon: ImageVector, title: String, desc: String, button: String, url: String) {
    val uri = LocalUriHandler.current
    val bob by rememberInfiniteTransition(label = "b").animateFloat(
        -8f, 8f, infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "by"
    )
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Appear(0) {
            Box(
                Modifier.graphicsLayer { translationY = bob }.size(96.dp).clip(CircleShape)
                    .background(Color(0x334FC3FF)).border(1.dp, Ice.copy(alpha = .6f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = Ice, modifier = Modifier.size(44.dp)) }
        }
        Spacer(Modifier.height(22.dp))
        Appear(1) { Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center) }
        Spacer(Modifier.height(18.dp))
        Appear(2) {
            GlassCard {
                Text(desc, color = Color.White.copy(alpha = .9f), fontSize = 14.sp, lineHeight = 21.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                GlowButton(button) { uri.openUri(url) }
            }
        }
    }
}

@Composable
fun ContactScreen() {
    val uri = LocalUriHandler.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Appear(0) { Text("CONTACT DEV", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold) }
        Appear(1) { Text("Hubungi developer lewat WhatsApp", color = Ice, fontSize = 14.sp) }
        Spacer(Modifier.height(20.dp))
        Cfg.CONTACTS.forEachIndexed { i, (label, num) ->
            Appear(i + 2) {
                Column {
                    GlassCard {
                        Text("${i + 1}. $label", color = Accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(pretty(num), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(14.dp))
                        GlowButton("Chat WhatsApp") { uri.openUri("https://wa.me/$num") }
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }
        }
    }
}

private fun pretty(n: String) = "+${n.take(2)} ${n.drop(2).chunked(4).joinToString("-")}"
