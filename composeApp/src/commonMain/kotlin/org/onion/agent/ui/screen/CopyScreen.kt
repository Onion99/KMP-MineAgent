package org.onion.agent.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.onion.agent.ui.navigation.route.DetailRoute
import org.onion.agent.ui.navigation.route.RootRoute


fun NavGraphBuilder.copyScreen(){
    composable(DetailRoute.Home.name) {
        Box(modifier = Modifier.background(Color.Blue).fillMaxSize())
    }
}