package org.onion.agent.ui.navigation.route

import mineagent.composeapp.generated.resources.Res
import mineagent.composeapp.generated.resources.chat
import mineagent.composeapp.generated.resources.home
import mineagent.composeapp.generated.resources.ic_chat
import mineagent.composeapp.generated.resources.ic_home
import mineagent.composeapp.generated.resources.ic_library
import mineagent.composeapp.generated.resources.ic_setting
import mineagent.composeapp.generated.resources.library
import mineagent.composeapp.generated.resources.setting
import kotlinx.serialization.Serializable


@Serializable
sealed interface MainRoute {
    @Serializable
    data object HomeRoute : RoutePage(Res.drawable.ic_home, Res.string.home)
    @Serializable
    data object ChatRoute : RoutePage(Res.drawable.ic_chat, Res.string.chat)
    @Serializable
    data object LibraryRoute : RoutePage(Res.drawable.ic_library, Res.string.library)
    @Serializable
    data object SettingRoute : RoutePage(Res.drawable.ic_setting, Res.string.setting)
}

val NAV_BOTTOM_ITEMS = listOf(
    MainRoute.HomeRoute,
    MainRoute.ChatRoute,
    MainRoute.LibraryRoute,
    MainRoute.SettingRoute
)