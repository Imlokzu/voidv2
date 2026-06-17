package com.maxrave.simpmusic.ui.navigation.graph

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.maxrave.simpmusic.ui.navigation.destination.import.ImportDestination
import com.maxrave.simpmusic.ui.screen.import.ImportTracksScreen

fun NavGraphBuilder.importScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
) {
    composable<ImportDestination> {
        ImportTracksScreen(
            navController = navController,
        )
    }
}