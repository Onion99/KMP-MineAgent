package org.onion.agent.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.onion.theme.helper.verticalSafePadding
import com.onion.theme.state.AdaptiveLayoutType
import com.onion.theme.style.glassSurface
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
import ui.theme.AppTheme


@Composable
fun MainScreen(
    onAdvancedSettingsClick: () -> Unit = {}
) {
    val backstack = remember { mutableStateListOf<Any>(MainRoute.HomeRoute) }
    val mainNavActions = remember(backstack) { NavActions(backstack) }

    MainContent(backstack, mainNavActions, onAdvancedSettingsClick)
}

@Composable
fun MainContent(
    backstack: MutableList<Any>,
    mainNavActions: NavActions,
    onAdvancedSettingsClick: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f)) {
            // ── Desktop/Tablet: Glassmorphism vertical navigation rail ──
            AnimatedVisibility(
                visible = AppTheme.adaptiveLayoutType == AdaptiveLayoutType.Medium
                        || AppTheme.adaptiveLayoutType == AdaptiveLayoutType.Expanded
            ) {
                val currentRoute = backstack.lastOrNull()
                EtherealNavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(
                            start = AppTheme.spacing.md,
                            top = AppTheme.spacing.md,
                            bottom = AppTheme.spacing.md
                        )
                        .padding(verticalSafePadding()),
                    selectedRoute = currentRoute,
                    onRouteSelected = { mainNavActions.popAndNavigation(it) },
                )
            }

            // ── Content area ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = AppTheme.size.maxContentWidth)
            ) {
                NavDisplay(
                    backStack = backstack,
                    onBack = { mainNavActions.back() }
                ) { key ->
                    when (key) {
                        is MainRoute.HomeRoute -> NavEntry(key) {
                            HomeScreen(
                                onSettingsClick = {
                                    mainNavActions.popAndNavigation(MainRoute.SettingRoute)
                                },
                                onAdvancedSettingsClick = onAdvancedSettingsClick
                            )
                        }

                        is MainRoute.ChatRoute -> NavEntry(key) {
                            ChatScreen()
                        }

                        is MainRoute.LibraryRoute -> NavEntry(key) {
                            LibraryScreen()
                        }

                        is MainRoute.SettingRoute -> NavEntry(key) {
                            SettingScreen(
                                onBackClick = {
                                    mainNavActions.popAndNavigation(MainRoute.HomeRoute)
                                }
                            )
                        }

                        else -> NavEntry(key) {
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Page not found: $key")
                            }
                        }
                    }
                }
            }
        }

        // ── Mobile: Glassmorphism bottom navigation bar ──
        AnimatedVisibility(
            visible = AppTheme.adaptiveLayoutType == AdaptiveLayoutType.Compact
        ) {
            val currentRoute = backstack.lastOrNull()
            EtherealBottomNavigationBar(
                selectedRoute = currentRoute,
                onRouteSelected = { mainNavActions.popAndNavigation(it) },
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// Desktop — Ethereal Glassmorphism Vertical Navigation Rail
// ════════════════════════════════════════════════════════════════════════════

@Composable
fun EtherealNavigationRail(
    modifier: Modifier,
    selectedRoute: Any?,
    onRouteSelected: (Any) -> Unit,
) {
    Column(
        modifier
            .glassSurface(
                shape = AppTheme.shape.full,
                alpha = AppTheme.elevation.glassSurfaceAlpha,
                borderAlpha = AppTheme.elevation.glassBorderAlpha
            )
            .widthIn(min = 72.dp)
            .padding(vertical = AppTheme.spacing.lg)
            .selectableGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
    ) {
        // ── Brand mark ──
        BrandMark()

        Spacer(Modifier.height(AppTheme.spacing.lg))

        // ── Scrollable nav items ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)
        ) {
            NAV_BOTTOM_ITEMS.forEach { routePage ->
                val isSelected = routePage == selectedRoute
                val iconAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.6f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "nav_icon_alpha"
                )

                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onRouteSelected(routePage) },
                    icon = {
                        Icon(
                            imageVector = vectorResource(routePage.iconRes),
                            contentDescription = stringResource(routePage.textRes),
                            modifier = Modifier
                                .size(AppTheme.size.iconLarge)
                                .alpha(iconAlpha)
                        )
                    },
                    colors = etherealRailItemColors()
                )
            }

            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = AppTheme.spacing.xl)
            )

            // ── Theme toggle ──
            ThemeToggleRailItem()
        }
    }
}

/**
 * Compact brand mark — a small gradient circle with initials,
 * sits at the top of the navigation rail.
 */
@Composable
private fun BrandMark() {
    Box(
        modifier = Modifier
            .size(AppTheme.size.iconButton)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        AppTheme.colors.primary,
                        AppTheme.colors.secondary
                    )
                ),
                shape = AppTheme.shape.full
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "A",
            style = AppTheme.typography.labelLarge,
            color = AppTheme.colors.onPrimary
        )
    }
}

/**
 * Theme toggle item at the bottom of the rail.
 */
@Composable
private fun ThemeToggleRailItem() {
    val isDark by AppTheme.isDark
    NavigationRailItem(
        selected = false,
        onClick = { /* Theme toggle handled externally */ },
        icon = {
            Icon(
                imageVector = vectorResource(
                    if (isDark) Res.drawable.ic_sun else Res.drawable.ic_moon
                ),
                contentDescription = stringResource(
                    if (isDark) Res.string.light_theme else Res.string.dark_theme
                ),
                modifier = Modifier.size(AppTheme.size.icon),
                tint = AppTheme.colors.onSurfaceVariant
            )
        },
        colors = etherealRailItemColors()
    )
}


// ════════════════════════════════════════════════════════════════════════════
// Mobile — Ethereal Glassmorphism Bottom Navigation Bar
// ════════════════════════════════════════════════════════════════════════════

@Composable
fun EtherealBottomNavigationBar(
    selectedRoute: Any?,
    onRouteSelected: (Any) -> Unit,
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = AppTheme.colors.surfaceContainerLow.copy(
            alpha = AppTheme.elevation.glassSurfaceAlpha
        ),
        tonalElevation = 0.dp
    ) {
        NAV_BOTTOM_ITEMS.forEach { routePage ->
            val isSelected = routePage == selectedRoute

            NavigationBarItem(
                selected = isSelected,
                onClick = { onRouteSelected(routePage) },
                icon = {
                    Icon(
                        imageVector = vectorResource(routePage.iconRes),
                        contentDescription = stringResource(routePage.textRes),
                        modifier = Modifier.size(AppTheme.size.iconLarge)
                    )
                },
                label = {
                    Text(
                        text = stringResource(routePage.textRes),
                        style = if (isSelected) AppTheme.typography.labelMedium
                        else AppTheme.typography.labelSmall
                    )
                },
                colors = etherealBarItemColors()
            )
        }
    }
}


// ════════════════════════════════════════════════════════════════════════════
// Shared — Color configurations for navigation items
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun etherealRailItemColors() = NavigationRailItemDefaults.colors(
    selectedIconColor = AppTheme.colors.onPrimaryContainer,
    selectedTextColor = AppTheme.colors.onSurface,
    indicatorColor = AppTheme.colors.primaryContainer.copy(alpha = 0.7f),
    unselectedIconColor = AppTheme.colors.onSurfaceVariant,
    unselectedTextColor = AppTheme.colors.onSurfaceVariant
)

@Composable
private fun etherealBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AppTheme.colors.onPrimaryContainer,
    selectedTextColor = AppTheme.colors.onSurface,
    indicatorColor = AppTheme.colors.primaryContainer.copy(alpha = 0.7f),
    unselectedIconColor = AppTheme.colors.onSurfaceVariant,
    unselectedTextColor = AppTheme.colors.onSurfaceVariant
)