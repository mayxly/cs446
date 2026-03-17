package com.builderbears.align.data.model

data class Activity (
    val activityId: String = "",
    val userId: String = "",
    val userName: String = "",
    val name: String = "",
    val description: String = "",
    val workoutType: String = "",
    val location: String = "",
    val date: String = "",
    val time: String = "",
    val invited: List<String> = emptyList(),
    val invitedNames: List<String> = emptyList(),
    val imageUrl: String? = null,
    val reactions: Map<String, List<String>> = emptyMap()
)
