package com.builderbears.align.ui.screens.feed

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.builderbears.align.data.model.Activity
import com.builderbears.align.ui.components.InboxScreen
import com.builderbears.align.ui.components.NotificationButton
import com.builderbears.align.ui.components.UserAvatar
import com.builderbears.align.ui.theme.BorderLight
import com.builderbears.align.ui.theme.CardWhite
import com.builderbears.align.ui.theme.GradientBlue
import com.builderbears.align.ui.theme.GradientMint
import com.builderbears.align.ui.theme.GradientPink
import com.builderbears.align.ui.theme.GradientYellow
import com.builderbears.align.ui.theme.Indigo
import com.builderbears.align.ui.theme.TextMuted
import com.builderbears.align.ui.theme.TextPrimary
import com.builderbears.align.ui.theme.TextSecondary
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun FeedScreen(viewModel: FeedViewModel = viewModel()) {
    val activities by viewModel.activities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showInbox by remember { mutableStateOf(false) }

    // Refresh on screen appear
    DisposableEffect(Unit) {
        viewModel.refreshActivities()
        onDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val w = size.width
                val h = size.height
                val radius = maxOf(w, h) * 0.85f

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(GradientBlue, Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = radius
                    ),
                    size = size
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(GradientPink, Color.Transparent),
                        center = Offset(w, 0f),
                        radius = radius
                    ),
                    size = size
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(GradientYellow, Color.Transparent),
                        center = Offset(0f, h),
                        radius = radius
                    ),
                    size = size
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(GradientMint, Color.Transparent),
                        center = Offset(w, h),
                        radius = radius
                    ),
                    size = size
                )
            }
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Past Workouts",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            NotificationButton(onClick = { showInbox = true })
        }

        HorizontalDivider(color = BorderLight, thickness = 1.dp, modifier = Modifier.padding(horizontal = 0.dp))

        Spacer(Modifier.height(8.dp))

        when {
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${error}",
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            activities.isEmpty() && isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Indigo)
                }
            }
            activities.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No activities yet",
                        color = TextMuted,
                        fontSize = 16.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activities) { activity ->
                        ActivityCard(
                            activity = activity,
                            onReactionClick = { emoji ->
                                val hasReacted = activity.reactions[emoji]?.contains(
                                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                ) == true
                                if (hasReacted) {
                                    viewModel.removeReaction(activity.activityId, emoji)
                                } else {
                                    viewModel.addReaction(activity.activityId, emoji)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showInbox) {
        InboxScreen(
            onDismiss = { showInbox = false }
        )
    }
}

@Composable
private fun ActivityCard(
    activity: Activity,
    onReactionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top row: participants and workout type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activity.participants.isNotEmpty()) {
                    ActivityMembers(
                        participants = activity.participants,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = "Shared workout",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F0F0))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activity.getWorkoutEmoji(),
                            fontSize = 12.sp
                        )
                        Text(
                            text = activity.workoutType,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "${formatActivityDate(activity.date)} • ${activity.time}",
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = activity.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!activity.imageUrl.isNullOrEmpty()) {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    // Placeholder
                    Text(
                        text = "[Image]",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            EmojiReactionBar(
                reactions = activity.reactions,
                defaultReactions = defaultReactions,
                onReactionClick = onReactionClick
            )
        }
    }
}

@Composable
private fun ActivityMembers(
    participants: List<com.builderbears.align.data.model.ActivityParticipant>,
    modifier: Modifier = Modifier
) {
    if (participants.isEmpty()) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box {
            Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                participants.forEach { participant ->
                    UserAvatar(
                        name = participant.name,
                        size = 20.dp,
                        userId = participant.userId,
                        profilePhotoUrl = participant.profilePhotoUrl.takeIf { it.isNotBlank() },
                        showShadow = false
                    )
                }
            }
        }

        Text(
            text = "with",
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Normal
        )

        val displayNames = participants.take(2)
            .map { it.name }
            .joinToString(", ")

        Text(
            text = if (participants.size > 2) "$displayNames, +${participants.size - 2} more" else displayNames,
            fontSize = 11.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EmojiReactionBar(
    reactions: Map<String, List<String>>,
    defaultReactions: List<String>,
    onReactionClick: (String) -> Unit
) {
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        defaultReactions.forEach { emoji ->
            val reactedUsers = reactions[emoji] ?: emptyList()
            val hasReacted = reactedUsers.contains(currentUserId)

            EmojiReactionButton(
                emoji = emoji,
                count = reactedUsers.size,
                isReacted = hasReacted,
                onClick = { onReactionClick(emoji) }
            )
        }
    }
}

@Composable
private fun EmojiReactionButton(
    emoji: String,
    count: Int,
    isReacted: Boolean,
    onClick: () -> Unit
) {
    val textColor by animateColorAsState(
        targetValue = if (isReacted) Indigo else TextMuted,
        label = "textColor"
    )

    Card(
        modifier = Modifier
            .height(28.dp)
            .wrapContentWidth()
            .border(1.dp, Color(0xFFD0D0D0), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 12.sp)
            if (count > 0) {
                Spacer(Modifier.width(3.dp))
                Text(
                    text = count.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}
