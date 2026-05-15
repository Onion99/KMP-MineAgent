package org.onion.agent.ui.navigation.route

import mineagent.composeapp.generated.resources.Res
import mineagent.composeapp.generated.resources.home
import mineagent.composeapp.generated.resources.ic_home
import kotlinx.serialization.Serializable


@Serializable
sealed interface MainRoute {
    @Serializable
    data object HomeRoute : RoutePage(Res.drawable.ic_home, Res.string.home)
    @Serializable
    data object SettingRoute : RoutePage(Res.drawable.ic_home, Res.string.home)
    @Serializable
    data object MineRoute : RoutePage(Res.drawable.ic_home, Res.string.home)
}
val NAV_BOTTOM_ITEMS = listOf(
    MainRoute.HomeRoute,
    MainRoute.SettingRoute,
    MainRoute.MineRoute
)