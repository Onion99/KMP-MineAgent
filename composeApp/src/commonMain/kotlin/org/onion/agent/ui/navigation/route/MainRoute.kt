package org.onion.agent.ui.navigation.route

import mineagent.composeapp.generated.resources.Res
import mineagent.composeapp.generated.resources.home
import mineagent.composeapp.generated.resources.ic_home


sealed interface MainRoute {
    data object HomeRoute : RoutePage(name = "HomeRoute",Res.drawable.ic_home, Res.string.home)
    data object SettingRoute : RoutePage(name = "SettingRoute",Res.drawable.ic_home, Res.string.home)
    data object MineRoute : RoutePage(name = "MineRoute",Res.drawable.ic_home, Res.string.home)
}
val NAV_BOTTOM_ITEMS = listOf(
    MainRoute.HomeRoute,
    MainRoute.SettingRoute,
    MainRoute.MineRoute
)