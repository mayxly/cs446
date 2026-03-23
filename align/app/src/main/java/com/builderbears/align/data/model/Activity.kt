package com.builderbears.align.data.model

data class ActivityParticipant(
    val userId: String = "",
    val name: String = ""
)

data class Activity (
    val activityId: String = "",
    val name: String = "",
    val description: String = "",
    val workoutType: String = "",
    val location: String = "",
    val date: String = "",
    val time: String = "",
    val participantIds: List<String> = emptyList(),
    val participants: List<ActivityParticipant> = emptyList(),
    val imageUrl: String? = null,
    val reactions: Map<String, List<String>> = emptyMap()
)
