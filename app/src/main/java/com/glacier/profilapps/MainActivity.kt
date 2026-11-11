package com.glacier.profilapps

import android.graphics.Color as AColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AColor.TRANSPARENT, AColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AColor.TRANSPARENT, AColor.TRANSPARENT)
        )
        setContent { GlacierTheme { Root() } }
    }
}

@Composable
fun Root() {
    var splash by rememberSaveable { mutableStateOf(true) }
    Box(Modifier.fillMaxSize().background(BgBase)) {
        Crossfade(splash, animationSpec = tween(700), label = "root") { s ->
            if (s) SplashScreen { splash = false } else MainScreen()
        }
    }
}

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(1.1f) }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(900)) }
        launch { scale.animateTo(1f, tween(2400, easing = IceBezier)) }
        launch { progress.animateTo(1f, tween(2100, easing = IceBezier)) }
        delay(2500)
        onDone()
    }
    PageBackground {
        val shape = RoundedCornerShape(26.dp)
        Image(
            painterResource(R.drawable.banner), null,
            Modifier.align(Alignment.Center).fillMaxWidth().padding(16.dp)
                .graphicsLayer { scaleX = scale.value; scaleY = scale.value; this.alpha = alpha.value }
                .shadow(14.dp, shape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(shape)
                .border(2.dp, WhiteRim, shape),
            contentScale = ContentScale.FillWidth
        )
        Column(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 56.dp, vertical = 48.dp)
                .graphicsLayer { this.alpha = alpha.value },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.fillMaxWidth().height(14.dp).clip(CircleShape).background(Color.White)
                    .inset(CircleShape, 2.dp).border(1.5.dp, IceBorder, CircleShape)
            ) {
                Box(
                    Modifier.fillMaxHeight().fillMaxWidth(progress.value).clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(IceRim, IceGlow, IcePrimary)))
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(Cfg.APP_NAME, color = IceDeep, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

private data class Tab(val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("Home", Ic.Home), Tab("Packages", Ic.Pkg), Tab("Cleaner", Ic.Brush),
    Tab("Saran", Ic.Shield), Tab("Profile", Ic.User),
)

private const val ACTIVE_W = 2.3f

@Composable
fun MainScreen() {
    val vm: MainVM = viewModel()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val pos by animateFloatAsState(tab.toFloat(), tween(400, easing = IceBezier), label = "pager")

    PageBackground {
        Box(Modifier.fillMaxSize().statusBarsPadding().clipToBounds()) {
            repeat(tabs.size) { i ->
                Box(Modifier.fillMaxSize().graphicsLayer { translationX = (i - pos) * size.width }) {
                    when (i) {
                        0 -> HomeScreen(vm)
                        1 -> PackagesScreen(vm)
                        2 -> CleanerScreen(vm, tab == 2)
                        3 -> SaranScreen(vm)
                        else -> ProfileScreen()
                    }
                }
            }
        }
        BottomNav(tab, { tab = it }, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 20.dp))
        IslandToast(vm)
    }
}

@Composable
fun BottomNav(tab: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier.fillMaxWidth(0.94f)
            .shadow(14.dp, CircleShape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xE6F0F9FF), Color(0xCCE0F2FE))))
            .inset(CircleShape, 5.dp).border(1.5.dp, IcePrimary, CircleShape).padding(5.dp)
    ) {
        val unit = maxWidth / (tabs.size - 1 + ACTIVE_W)
        val x by animateDpAsState(unit * tab, spring(dampingRatio = 0.8f, stiffness = 300f), label = "navx")
        Box(
            Modifier.offset(x = x).size(unit * ACTIVE_W, 40.dp)
                .shadow(5.dp, CircleShape, ambientColor = IcePrimary, spotColor = IcePrimary).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF0EA5E9), IcePrimary)))
        )
        Row(Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { i, t ->
                val sel = tab == i
                val w by animateFloatAsState(if (sel) ACTIVE_W else 1f, spring(dampingRatio = 0.8f, stiffness = 300f), label = "navw")
                val fg by animateColorAsState(if (sel) Color.White else TextMuted, tween(300), label = "navc")
                Row(
                    Modifier.weight(w).height(40.dp).clip(CircleShape).clickable { onSelect(i) },
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
                ) {
                    I(t.icon, fg, 16.dp)
                    AnimatedVisibility(sel, enter = fadeIn() + expandHorizontally(), exit = fadeOut() + shrinkHorizontally()) {
                        Text(
                            t.label, color = fg, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold,
                            maxLines = 1, softWrap = false, modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
