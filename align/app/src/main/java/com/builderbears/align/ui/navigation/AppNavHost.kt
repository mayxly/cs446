package com.builderbears.align.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*

import com.builderbears.align.ui.screens.addactivity.AddActivityScreen
import com.builderbears.align.ui.screens.feed.FeedScreen
import com.builderbears.align.ui.screens.login.LoginScreen
import com.builderbears.align.ui.screens.schedule.ScheduleScreen
import com.builderbears.align.ui.screens.you.YouScreen
import com.builderbears.align.ui.theme.NavBarBackground
import com.builderbears.align.ui.theme.NavBarHighlight
import com.builderbears.align.ui.theme.NavBarSelected
import com.builderbears.align.ui.theme.NavBarUnselected
import com.google.firebase.auth.FirebaseAuth

private data class NavItem(
    val route: Route,
    val icon: ImageVector,
    val label: String
)

private val navItems = listOf(
    NavItem(Route.Feed, Icons.Outlined.GridView, "FEED"),
    NavItem(Route.Schedule, Icons.Outlined.CalendarMonth, "SCHEDULE"),
    NavItem(Route.AddActivity, Icons.Outlined.AddCircleOutline, "ADD"),
    NavItem(Route.You, Icons.Outlined.PersonOutline, "YOU")
)

@Composable
fun AlignApp() {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if logged in, if not then go to LoginScreen
    var isLoggedIn by remember {
        mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
    }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { isLoggedIn = true }
        )
        return
    }

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
                composable(Route.AddActivity.path) { AddActivityScreen(navController) }
                composable(Route.Feed.path) { FeedScreen() }
                composable(Route.Schedule.path) { ScheduleScreen() }
                composable(Route.You.path) { YouScreen(onLogout = { isLoggedIn = false }) }
            }
        }

        // Bottom navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(NavBarBackground)
                .padding(top = 12.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentRoute == item.route.path
                BottomNavItem(
                    icon = item.icon,
                    label = item.label,
                    isSelected = isSelected,
                    onClick = {
                        navController.navigate(item.route.path) {
                            popUpTo(Route.Feed.path) { saveState = true }
                            launchSingleTop = true
                            // Force Schedule to recreate so it refetches before showing.
                            restoreState = item.route != Route.Schedule
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) NavBarSelected else NavBarUnselected

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // Icon with pill highlight when selected
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(32.dp)
                .widthIn(min = 56.dp)
                .then(
                    if (isSelected) {
                        Modifier.background(NavBarHighlight, RoundedCornerShape(16.dp))
                    } else {
                        Modifier
                    }
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Label
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Selection dot
        Box(
            modifier = Modifier
                .size(6.dp)
                .then(
                    if (isSelected) {
                        Modifier.background(NavBarSelected, CircleShape)
                    } else {
                        Modifier
                    }
                )
        )
    }
}
