package org.onion.agent.ui.navigation.route

import mineagent.composeapp.generated.resources.Res
import mineagent.composeapp.generated.resources.ic_help
import mineagent.composeapp.generated.resources.unknown
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import kotlinx.serialization.Serializable


@Serializable
sealed interface RootRoute {
    @Serializable
    data object Splash : RoutePage()
    @Serializable
    data object MainRoute : RoutePage()
    @Serializable
    data object SettingRoute : RoutePage()
    @Serializable
    data object AdvancedSettingRoute : RoutePage()
}

@Serializable
open class RoutePage(
    val iconRes: DrawableResource = Res.drawable.ic_help,
    val textRes: StringResource = Res.string.unknown
)