package org.onion.agent.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.onion.theme.helper.verticalSafePadding
import com.onion.theme.state.AdaptiveLayoutType
import mineagent.composeapp.generated.resources.Res
import mineagent.composeapp.generated.resources.dark_theme
import mineagent.composeapp.generated.resources.ic_moon
import mineagent.composeapp.generated.resources.ic_sun
import mineagent.composeapp.generated.resources.light_theme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.onion.agent.ui.navigation.NavActions
import org.onion.agent.ui.navigation.route.MainRoute
import org.onion.agent.ui.navigation.route.NAV_BOTTOM_ITEMS
import org.onion.agent.ui.navigation.route.RootRoute
import org.onion.agent.ui.screen.CopyScreen
import org.onion.agent.ui.screen.HomeScreen
import org.onion.agent.ui.screen.SettingScreen
import ui.theme.AppTheme


@Composable
fun MainScreen(
    onAdvancedSettingsClick: () -> Unit = {}
) {
    val backstack = remember { mutableStateListOf<Any>(MainRoute.HomeRoute) }
    val mainNavActions = remember(backstack) { NavActions(backstack) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    ModalNavigationDrawer(
        drawerContent = {
            ModalNavigationContent()
        },
        drawerState = drawerState,
        gesturesEnabled = false
    ) {
        MainContent(backstack, mainNavActions, onAdvancedSettingsClick)
    }
}

val navigationDrawerMinWidth = 240.dp
val navigationDrawerMaxWidth = 360.dp

@Composable
fun ModalNavigationContent() {
    Column(
        modifier = Modifier.fillMaxHeight().padding(AppTheme.spacing.s200)
            .clip(AppTheme.shape.r500)
            .widthIn(min = navigationDrawerMinWidth, max = navigationDrawerMaxWidth)
            .border(AppTheme.size.borderWidth, AppTheme.colors.border, AppTheme.shape.r500)
            .background(AppTheme.colors.surfaceContainer)
            .padding(horizontal = AppTheme.spacing.s400, vertical = AppTheme.spacing.s450)
    ) {

    }
}

@Composable
fun MainContent(
    backstack: MutableList<Any>,
    mainNavActions: NavActions,
    onAdvancedSettingsClick: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f)) {
            AnimatedVisibility(
                visible = AppTheme.adaptiveLayoutType == AdaptiveLayoutType.Medium || AppTheme.adaptiveLayoutType == AdaptiveLayoutType.Expanded
            ) {
                val currentRoute = backstack.lastOrNull()
                SlideNavigationBar(
                    modifier = Modifier.fillMaxHeight().padding(start = AppTheme.spacing.s300)
                        .padding(verticalSafePadding()),
                    selectedRoute = currentRoute,
                    onRouteSelected = { mainNavActions.popAndNavigation(it) },
                    onThemeChanged = {}
                )
            }
            Box(modifier = Modifier.weight(1f).widthIn(max = AppTheme.size.maxContainerWidth)) {
                NavDisplay(
                    backStack = backstack,
                    onBack = { mainNavActions.back() }
                ) { key ->
                    when (key) {
                        is MainRoute.HomeRoute -> NavEntry(key) {
                            HomeScreen(
                                onSettingsClick = { mainNavActions.navigationTo(MainRoute.SettingRoute) },
                                onAdvancedSettingsClick = onAdvancedSettingsClick
                            )
                        }
                        is MainRoute.SettingRoute -> NavEntry(key) {
                            SettingScreen(
                                onBackClick = { mainNavActions.back() }
                            )
                        }
                        is MainRoute.MineRoute -> NavEntry(key) {
                            CopyScreen()
                        }
                        else -> NavEntry(key) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Page not found: $key")
                            }
                        }
                    }
                }
            }
        }
        AnimatedVisibility(visible = AppTheme.adaptiveLayoutType == AdaptiveLayoutType.Compact) {
            val currentRoute = backstack.lastOrNull()
            BottomNavigationBar(
                selectedRoute = currentRoute,
                onRouteSelected = { mainNavActions.popAndNavigation(it) },
                onThemeChanged = {}
            )
        }
    }
}

@Composable
fun SlideNavigationBar(
    modifier: Modifier,
    selectedRoute: Any?,
    onRouteSelected: (Any) -> Unit,
    onThemeChanged: () -> Unit
) {
    Column(
        modifier
            .border(AppTheme.size.borderWidth, AppTheme.colors.border, CircleShape)
            .background(AppTheme.colors.surfaceContainer, CircleShape).widthIn(min = 80.dp)
            .padding(vertical = AppTheme.spacing.s400).selectableGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s200)
    ) {
        Spacer(Modifier.height(AppTheme.spacing.s300))
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s200)
        ) {
            NAV_BOTTOM_ITEMS.forEach { routePage ->
                val isSelected = routePage == selectedRoute
                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onRouteSelected(routePage) },
                    icon = {
                        Icon(
                            imageVector = vectorResource(routePage.iconRes),
                            contentDescription = stringResource(routePage.textRes)
                        )
                    },
                    colors = navigationRailItemColors()
                )
            }

            Spacer(modifier = Modifier.weight(1f).heightIn(min = AppTheme.spacing.s400))
            val isDark by AppTheme.isDark
            NavigationRailItem(selected = false, onClick = {
                onThemeChanged()
            }, icon = {
                Icon(
                    imageVector = vectorResource(if (isDark) Res.drawable.ic_sun else Res.drawable.ic_moon),
                    contentDescription = stringResource(if (isDark) Res.string.light_theme else Res.string.dark_theme),
                    tint = AppTheme.colors.onSurfaceVariant
                )
            })
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedRoute: Any?,
    onRouteSelected: (Any) -> Unit,
    onThemeChanged: () -> Unit
) {
    NavigationBar(modifier = Modifier.fillMaxWidth(), containerColor = AppTheme.colors.surface) {
        NAV_BOTTOM_ITEMS.forEach { routePage ->
            val isSelected = routePage == selectedRoute
            NavigationBarItem(
                selected = isSelected,
                onClick = { onRouteSelected(routePage) },
                icon = {
                    Icon(
                        imageVector = vectorResource(routePage.iconRes),
                        contentDescription = stringResource(routePage.textRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(routePage.textRes),
                        style = if (isSelected) AppTheme.typography.labelMedium else AppTheme.typography.labelSmall
                    )
                },
                colors = navigationBarItemColors()
            )
        }
    }
}

@Composable
private fun navigationRailItemColors() = NavigationRailItemDefaults.colors(
    selectedIconColor = AppTheme.colors.onPrimaryContainer,
    selectedTextColor = AppTheme.colors.onSurfaceContainer,
    indicatorColor = AppTheme.colors.primaryContainer,
    unselectedIconColor = AppTheme.colors.onSurfaceVariant,
    unselectedTextColor = AppTheme.colors.onSurfaceVariant
)

@Composable
private fun navigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AppTheme.colors.onPrimaryContainer,
    selectedTextColor = AppTheme.colors.onSurface,
    indicatorColor = AppTheme.colors.primaryContainer,
    unselectedIconColor = AppTheme.colors.onSurfaceVariant,
    unselectedTextColor = AppTheme.colors.onSurfaceVariant
)