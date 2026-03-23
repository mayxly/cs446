package com.builderbears.align.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.builderbears.align.ui.components.InboxScreen
import com.builderbears.align.ui.components.NotificationCountBadge
import com.builderbears.align.data.model.Attendee
import com.builderbears.align.data.model.MonthGroup
import com.builderbears.align.data.model.ScheduledDay
import com.builderbears.align.data.model.WorkoutEvent
import com.builderbears.align.ui.theme.BorderLight
import com.builderbears.align.ui.theme.CardWhite
import com.builderbears.align.ui.theme.GradientBlue
import com.builderbears.align.ui.theme.GradientMint
import com.builderbears.align.ui.theme.GradientPink
import com.builderbears.align.ui.theme.GradientYellow
import com.builderbears.align.ui.theme.PrimaryBlue
import com.builderbears.align.ui.theme.TextPrimary
import com.builderbears.align.ui.theme.TextSecondary
import com.builderbears.align.ui.theme.TextMuted


@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = viewModel()) {
    val uiState = viewModel.uiState
    var showInbox by remember { mutableStateOf(false) }

    // Refresh immediately on entering the screen to avoid showing stale data.
    LaunchedEffect(Unit) {
        viewModel.reload()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val w = size.width
                val h = size.height
                val radius = maxOf(w, h) * 0.85f
                listOf(
                    GradientBlue  to Offset(0f, 0f),
                    GradientPink  to Offset(w, 0f),
                    GradientYellow to Offset(0f, h),
                    GradientMint  to Offset(w, h)
                ).forEach { (color, center) ->
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(color.copy(alpha = 0.4f), Color.Transparent),
                            center = center,
                            radius = radius
                        ),
                        size = size
                    )
                }
            }
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Box {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardWhite,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { showInbox = true }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Inbox",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                NotificationCountBadge(
                    count = 3,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                )
            }
        }

        HorizontalDivider(color = BorderLight, thickness = 1.dp, modifier = Modifier.padding(horizontal = 0.dp))

        Spacer(Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            uiState.error != null -> {
                ErrorState(message = uiState.error ?: "Unknown error") {
                    viewModel.reload()
                }
            }

            uiState.groups.isEmpty() -> {
                EmptyState()
            }

            else -> {
                uiState.groups.forEach { group ->
                    MonthSection(group)
                }
                Spacer(Modifier.height(100.dp))
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
private fun MonthSection(group: MonthGroup) {
    Text(
        text = group.month,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color = TextSecondary,
        modifier = Modifier.padding(start = 20.dp, bottom = 12.dp)
    )

    group.days.forEach { day ->
        DayRow(day)
        Spacer(Modifier.height(16.dp))
    }

    Spacer(Modifier.height(8.dp))
}

@Composable
private fun DayRow(day: ScheduledDay) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Date column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(52.dp)
        ) {
            Text(
                text = day.dayOfWeekLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(4.dp))
            if (day.isHighlighted) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${day.dayNumber}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            } else {
                Text(
                    text = "${day.dayNumber}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        EventCard(event = day.event, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun EventCard(event: WorkoutEvent, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = event.typeColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(event.typeEmoji, fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = event.type,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            val timeLabel = listOfNotNull(
                event.time.takeIf { it.isNotBlank() },
                event.duration?.takeIf { it.isNotBlank() }
            ).joinToString("  ")
            if (timeLabel.isNotBlank()) {
                Text(
                    text = timeLabel,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = event.location,
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)

            Spacer(Modifier.height(10.dp))

            if (event.attendees.isNotEmpty()) {
                EventAttendeesSummary(
                    attendees = event.attendees,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun EventAttendeesSummary(attendees: List<Attendee>, modifier: Modifier = Modifier) {
    if (attendees.isEmpty()) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.height(20.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                attendees.forEach { attendee ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(attendee.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = attendee.initials,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Text(
            text = "with",
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Normal
        )

        val displayNames = attendees.take(2).joinToString(", ") { it.name }
        Text(
            text = if (attendees.size > 2) "$displayNames, +${attendees.size - 2} more" else displayNames,
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
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Retry", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No upcoming activities yet",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Add a workout to see it here.",
            color = TextSecondary,
            fontSize = 13.sp
        )
    }
}