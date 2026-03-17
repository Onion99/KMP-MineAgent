package org.onion.agent.ui.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import org.onion.agent.ui.navigation.NavActions
import org.onion.agent.ui.navigation.route.DetailRoute
import org.onion.agent.ui.navigation.route.MainRoute
import org.onion.agent.ui.screen.homeScreen

fun NavGraphBuilder.homeNavGraph(navActions: NavActions) {
    navigation( DetailRoute.Home.name,MainRoute.HomeRoute.name){
        homeScreen()
    }
}