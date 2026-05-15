package org.onion.agent

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import coil3.compose.setSingletonImageLoaderFactory
import com.onion.network.di.networkModule
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.onion.agent.di.viewModelModule
import org.onion.agent.ui.navigation.NavActions
import org.onion.agent.ui.navigation.route.MainRoute
import org.onion.agent.ui.navigation.route.RootRoute
import org.onion.agent.ui.screen.MainScreen
import org.onion.agent.ui.screen.SettingScreen
import org.onion.agent.ui.screen.AdvancedSettingScreen
import org.onion.agent.ui.screen.SplashScreen
import org.onion.agent.utils.imageLoaderDiskCache
import ui.theme.AppTheme

@Composable
@Preview
fun App() {
    KoinApplication({
        modules(viewModelModule, networkModule)
    }){
        // Initialize the Coil3 image loader
        setSingletonImageLoaderFactory { context ->
            imageLoaderDiskCache(context)
        }
        AppTheme {
            val backstack = remember { mutableStateListOf<Any>(RootRoute.Splash) }
            val rootNavActions = remember(backstack) {
                NavActions(backstack)
            }
            NavDisplay(
                backStack = backstack,
                onBack = { rootNavActions.back() }
            ) { key ->
                when (key) {
                    RootRoute.Splash -> NavEntry(key) {
                        SplashScreen(
                            autoToMainPage = { rootNavActions.popAndNavigation(MainRoute.HomeRoute) }
                        )
                    }
                    MainRoute.HomeRoute -> NavEntry(key) {
                        MainScreen(
                            onAdvancedSettingsClick = { rootNavActions.navigationTo(RootRoute.AdvancedSettingRoute) }
                        )
                    }
                    RootRoute.SettingRoute -> NavEntry(key) {
                        SettingScreen(
                            onBackClick = { rootNavActions.back() }
                        )
                    }
                    RootRoute.AdvancedSettingRoute -> NavEntry(key) {
                        AdvancedSettingScreen(
                            onBackClick = { rootNavActions.back() }
                        )
                    }
                    else -> NavEntry(key) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Route not found: $key")
                        }
                    }
                }
            }
        }
    }
}