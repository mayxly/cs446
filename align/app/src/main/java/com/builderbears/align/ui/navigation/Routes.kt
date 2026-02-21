package com.builderbears.align.ui.navigation

sealed class Route(val path: String) {
    data object AddActivity : Route("addactivity")
    data object Feed : Route("feed")
    data object Schedule : Route("schedule")
    data object You : Route("you")
}