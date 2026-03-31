package com.builderbears.align.data.model

data class ActivityParticipant(
    val userId: String = "",
    val name: String = "",
    val profilePhotoUrl: String = ""
)

data class Activity (
    val activityId: String = "",
    val name: String = "",
    val description: String = "",
    val workoutType: String = "",
    val location: String = "",
    val locationDisplayName: String = "",
    val locationDisplayAddress: String = "",
    val locationPlaceId: String = "",
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val date: String = "",
    val time: String = "",
    val participantIds: List<String> = emptyList(),
    val participants: List<ActivityParticipant> = emptyList(),
    val isPosted: Boolean = false,
    val imageUrl: String? = null,
    val reactions: Map<String, List<String>> = emptyMap()
)
