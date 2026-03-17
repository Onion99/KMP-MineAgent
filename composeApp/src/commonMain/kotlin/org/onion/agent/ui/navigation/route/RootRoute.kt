package org.onion.agent.ui.navigation.route

import androidx.navigation.NamedNavArgument
import mineagent.composeapp.generated.resources.Res
import mineagent.composeapp.generated.resources.ic_help
import mineagent.composeapp.generated.resources.unknown
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource


sealed interface RootRoute {
    data object Splash : RoutePage(name = "Splash")
    data object MainRoute : RoutePage(name = "MainRoute")
    data object SettingRoute : RoutePage(name = "SettingRoute")
    data object AdvancedSettingRoute : RoutePage(name = "AdvancedSettingRoute")
}

open class RoutePage(
    val name: String,
    val iconRes: DrawableResource = Res.drawable.ic_help,
    val textRes: StringResource = Res.string.unknown,
    val navArguments: List<NamedNavArgument> = emptyList()
)