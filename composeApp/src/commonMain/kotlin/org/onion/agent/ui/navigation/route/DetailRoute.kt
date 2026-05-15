package org.onion.agent.ui.navigation.route

import mineagent.composeapp.generated.resources.Res
import mineagent.composeapp.generated.resources.home
import mineagent.composeapp.generated.resources.ic_home
import kotlinx.serialization.Serializable


@Serializable
sealed interface DetailRoute {
    @Serializable
    data object Home : RoutePage(Res.drawable.ic_home, Res.string.home)
    @Serializable
    data object Setting : RoutePage(Res.drawable.ic_home, Res.string.home)
    @Serializable
    data object Mine : RoutePage(Res.drawable.ic_home, Res.string.home)
    @Serializable
    data object BookSource : RoutePage(Res.drawable.ic_home, Res.string.home)
}