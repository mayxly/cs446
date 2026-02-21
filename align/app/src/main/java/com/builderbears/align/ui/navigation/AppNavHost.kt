package com.builderbears.align.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*

import com.builderbears.align.ui.screens.addactivity.AddActivityScreen
import com.builderbears.align.ui.screens.feed.FeedScreen
import com.builderbears.align.ui.screens.schedule.ScheduleScreen
import com.builderbears.align.ui.screens.you.YouScreen

@Composable
fun AlignApp() {

    val navController = rememberNavController()

    Column(modifier = Modifier.fillMaxSize()) {

        // Main screen area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            NavHost(
                navController = navController,
                startDestination = Route.Feed.path
            ) {
                composable(Route.AddActivity.path) { AddActivityScreen() }
                composable(Route.Feed.path) { FeedScreen() }
                composable(Route.Schedule.path) { ScheduleScreen() }
                composable(Route.You.path) { YouScreen() }
            }
        }

        // Bottom bar with 4 circles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                Modifier.size(24.dp).background(Color.Black, CircleShape)
                    .clickable { navController.navigate(Route.Feed.path) }
            )

            Box(
                Modifier.size(24.dp).background(Color.Black, CircleShape)
                    .clickable { navController.navigate(Route.Schedule.path) }
            )

            Box(
                Modifier.size(24.dp).background(Color.Black, CircleShape)
                    .clickable { navController.navigate(Route.AddActivity.path) }
            )

            Box(
                Modifier.size(24.dp).background(Color.Black, CircleShape)
                    .clickable { navController.navigate(Route.You.path) }
            )
        }
    }
}