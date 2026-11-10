package com.glacier.profilapps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { GlacierTheme { Root() } }
    }
}

@Composable
fun Root() {
    var splash by rememberSaveable { mutableStateOf(true) }
    Box(Modifier.fillMaxSize().background(Bg)) {
        Crossfade(splash, animationSpec = tween(700), label = "root") { s ->
            if (s) SplashScreen { splash = false } else MainScreen()
        }
    }
}

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(1.12f) }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(900)) }
        launch { scale.animateTo(1f, tween(2400, easing = FastOutSlowInEasing)) }
        launch { progress.animateTo(1f, tween(2100, easing = FastOutSlowInEasing)) }
        delay(2500)
        onDone()
    }
    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.banner), null, Modifier.fillMaxSize().graphicsLayer { this.alpha = .35f },
            contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Bg.copy(alpha = .4f), Bg))))
        Snow(Modifier.fillMaxSize())
        Image(
            painterResource(R.drawable.banner), null,
            Modifier.align(Alignment.Center).fillMaxWidth().padding(16.dp)
                .graphicsLayer { scaleX = scale.value; scaleY = scale.value; this.alpha = alpha.value }
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.FillWidth
        )
        Column(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 56.dp, vertical = 48.dp)
                .graphicsLayer { this.alpha = alpha.value },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { progress.value }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = Accent, trackColor = Color(0x33BFE6FF)
            )
            Spacer(Modifier.height(12.dp))
            Text(Cfg.APP_NAME, color = Ice, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private data class Tab(val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("Home", Icons.Filled.Home),
    Tab("Packages", Icons.AutoMirrored.Filled.List),
    Tab("Cleaner", Icons.Filled.Delete),
    Tab("Saran", Icons.Filled.Lock),
    Tab("Profile", Icons.Filled.Person),
)

@Composable
fun MainScreen() {
    val vm: MainVM = viewModel()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Bg, Color(0xFF0A1A44), Bg)))) {
        Snow(Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    Modifier.statusBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(painterResource(R.drawable.logo), null, Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)))
                    Spacer(Modifier.width(10.dp))
                    Text(Cfg.APP_NAME, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Color(0xCC0A1433), tonalElevation = 0.dp) {
                    tabs.forEachIndexed { i, t ->
                        val sel = tab == i
                        val s by animateFloatAsState(
                            if (sel) 1.25f else 1f, spring(dampingRatio = .45f, stiffness = 380f), label = "ic"
                        )
                        NavigationBarItem(
                            selected = sel, onClick = { tab = i },
                            icon = { Icon(t.icon, t.label, Modifier.graphicsLayer { scaleX = s; scaleY = s }) },
                            label = { Text(t.label, fontSize = 10.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White, selectedTextColor = Accent,
                                indicatorColor = Color(0x554FC3FF),
                                unselectedIconColor = Ice.copy(alpha = .6f), unselectedTextColor = Ice.copy(alpha = .6f)
                            )
                        )
                    }
                }
            }
        ) { pad ->
            AnimatedContent(
                targetState = tab, modifier = Modifier.padding(pad).fillMaxSize(), label = "tabs",
                transitionSpec = {
                    val d = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(tween(420, easing = FastOutSlowInEasing)) { it / 4 * d } + fadeIn(tween(420))) togetherWith
                        (slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { -it / 4 * d } + fadeOut(tween(220)))
                }
            ) { t ->
                when (t) {
                    0 -> HomeScreen(vm)
                    1 -> PackagesScreen(vm)
                    2 -> CleanerScreen(vm)
                    3 -> SaranScreen(vm)
                    else -> ProfileScreen()
                }
            }
        }
        IslandToast(vm)
    }
}
