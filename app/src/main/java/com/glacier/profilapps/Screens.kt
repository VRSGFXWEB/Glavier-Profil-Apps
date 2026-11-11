package com.glacier.profilapps

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen() {
    val uri = LocalUriHandler.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp, 16.dp, 16.dp, 110.dp)) {
        HeaderCapsule(Ic.User, "Glacier Profile", "Developer Info")
        Appear(0) {
            val shape = RoundedCornerShape(26.dp)
            Box(
                Modifier.fillMaxWidth().padding(bottom = 14.dp).aspectRatio(16f / 9f)
                    .shadow(12.dp, shape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(shape).border(2.dp, WhiteRim, shape)
            ) { Image(painterResource(R.drawable.banner), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        }
        Appear(1) {
            Box(Modifier.padding(bottom = 12.dp)) {
                GlassBox(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painterResource(R.drawable.logo), null,
                            Modifier.size(56.dp).shadow(4.dp, CircleShape, ambientColor = ShadowIce, spotColor = ShadowIce)
                                .clip(CircleShape).border(2.dp, IcePrimary, CircleShape)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(Cfg.PROFILE_NAME, color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Text(Cfg.PROFILE_TAGLINE, color = IcePrimary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(Cfg.PROFILE_BIO, color = TextMain, fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Appear(2) {
            Box(Modifier.padding(bottom = 12.dp)) {
                PitBox {
                    TableHead("Saluran") { Text("WhatsApp", color = TextMain, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) }
                    LinkRow(Ic.Bell, "Saluran Admin", "Info, update & pengumuman", "Buka") { uri.openUri(Cfg.ADMIN_CHANNEL) }
                    LinkRow(Ic.Heart, "Thanks Support", "Terima kasih atas support", "Buka") { uri.openUri(Cfg.SUPPORT_CHANNEL) }
                }
            }
        }
        Appear(3) {
            PitBox {
                TableHead("Contact Dev") { Text("WhatsApp", color = TextMain, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) }
                Cfg.CONTACTS.forEachIndexed { i, (label, num) ->
                    LinkRow(Ic.Call, "${i + 1}. $label", pretty(num), "Chat") { uri.openUri("https://wa.me/$num") }
                }
            }
        }
    }
}

@Composable
private fun LinkRow(icon: ImageVector, title: String, sub: String, btn: String, onClick: () -> Unit) {
    CapsuleRow(height = 56.dp) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(IceLight).border(1.5.dp, IceBorder, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) { I(icon, IceDeep, 16.dp) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Text(sub, color = IceDeep, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        Spacer(Modifier.width(8.dp))
        Btn(btn, BtnKind.Success, onClick = onClick)
    }
}

private fun pretty(n: String) = "+${n.take(2)} ${n.drop(2).chunked(4).joinToString("-")}"
