package com.builderbears.align.ui.screens.schedule

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.builderbears.align.ui.screens.addactivity.AddActivityScreen
import com.builderbears.align.ui.components.InboxScreen
import com.builderbears.align.ui.components.UserAvatar
import com.builderbears.align.ui.components.NotificationCountBadge
import com.builderbears.align.ui.screens.you.InboxViewModel
import com.builderbears.align.data.model.Attendee
import com.builderbears.align.data.model.MonthGroup
import com.builderbears.align.data.model.ScheduledDay
import com.builderbears.align.data.model.WorkoutEvent
import com.builderbears.align.ui.theme.BorderLight
import com.builderbears.align.ui.theme.BorderMuted
import com.builderbears.align.ui.theme.CardWhite
import com.builderbears.align.ui.theme.Caption
import com.builderbears.align.ui.theme.DestructiveAction
import com.builderbears.align.ui.theme.DestructiveText
import com.builderbears.align.ui.theme.DisplayStyle
import com.builderbears.align.ui.theme.GradientBlue
import com.builderbears.align.ui.theme.GradientMint
import com.builderbears.align.ui.theme.GradientPink
import com.builderbears.align.ui.theme.GradientYellow
import com.builderbears.align.ui.theme.HeadingStyle2
import com.builderbears.align.ui.theme.HeadingStyle3
import com.builderbears.align.ui.theme.InputBackground
import com.builderbears.align.ui.theme.LabelLarge
import com.builderbears.align.ui.theme.LabelMedium
import com.builderbears.align.ui.theme.LabelSmall
import com.builderbears.align.ui.theme.NeutralActionBackground
import com.builderbears.align.ui.theme.NeutralActionText
import com.builderbears.align.ui.theme.PrimaryBlue
import com.builderbears.align.ui.theme.TextPrimary
import com.builderbears.align.ui.theme.TextSecondary
import com.builderbears.align.ui.theme.TextMuted
import com.builderbears.align.ui.utils.buildParticipantNamesAnnotated


@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = viewModel(),
    inboxViewModel: InboxViewModel
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val unreadCount by inboxViewModel.unreadCount.collectAsState()
    var showInbox by remember { mutableStateOf(false) }
    var expandedMenuEventId by remember { mutableStateOf<String?>(null) }
    var editingActivityId by remember { mutableStateOf<String?>(null) }
    var leaveConfirmActivityId by remember { mutableStateOf<String?>(null) }

    // Refresh immediately on entering the screen to avoid showing stale data.
    LaunchedEffect(Unit) {
        viewModel.reload()
    }

    LaunchedEffect(viewModel.actionError) {
        val message = viewModel.actionError ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.clearActionError()
    }

    LaunchedEffect(viewModel.actionMessage) {
        val message = viewModel.actionMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.consumeActionMessage()
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
                .padding(start = 20.dp, end = 16.dp, top = 36.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming",
                style = DisplayStyle.copy(fontSize = 28.sp, color = TextPrimary)
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
                if (unreadCount > 0) {
                    NotificationCountBadge(
                        count = unreadCount,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                    )
                }
            }
        }

        HorizontalDivider(
            color = BorderMuted,
            thickness = 1.dp,
            modifier = Modifier
                .padding(start = 20.dp)
                .fillMaxWidth(0.66f)
        )

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
                    MonthSection(
                        group = group,
                        expandedMenuEventId = expandedMenuEventId,
                        onMenuExpanded = { eventId ->
                            expandedMenuEventId = if (expandedMenuEventId == eventId) null else eventId
                        },
                        onMenuDismiss = {
                            expandedMenuEventId = null
                        },
                        onLeaveWorkout = { eventId ->
                            expandedMenuEventId = null
                            leaveConfirmActivityId = eventId
                        },
                        onEditWorkout = { eventId ->
                            expandedMenuEventId = null
                            editingActivityId = eventId
                        },
                        isActionInProgress = viewModel.isActionInProgress
                    )
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }

    if (showInbox) {
        InboxScreen(
            onDismiss = {
                showInbox = false
            },
            inboxViewModel = inboxViewModel
        )
    }

    editingActivityId?.let { activityId ->
        Dialog(
            onDismissRequest = { editingActivityId = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = InputBackground)
            ) {
                AddActivityScreen(
                    activityId = activityId,
                    isModal = true,
                    onDismissRequest = {
                        editingActivityId = null
                        viewModel.reload()
                    },
                    inboxViewModel = inboxViewModel
                )
            }
        }
    }

    leaveConfirmActivityId?.let { activityId ->
        LeaveWorkoutConfirmationDialog(
            isLoading = viewModel.isActionInProgress,
            onDismiss = {
                if (!viewModel.isActionInProgress) {
                    leaveConfirmActivityId = null
                }
            },
            onConfirm = {
                viewModel.leaveWorkout(activityId)
                leaveConfirmActivityId = null
            }
        )
    }
}

@Composable
private fun LeaveWorkoutConfirmationDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Are you sure you want to leave the\nworkout?",
                    style = HeadingStyle2.copy(
                        color = TextPrimary,
                        fontSize = 17.sp,
                        lineHeight = 23.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DestructiveAction)
                    ) {
                        Text("Leave", color = CardWhite, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeutralActionBackground)
                    ) {
                        Text("Cancel", color = NeutralActionText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSection(
    group: MonthGroup,
    expandedMenuEventId: String?,
    onMenuExpanded: (String) -> Unit,
    onMenuDismiss: () -> Unit,
    onLeaveWorkout: (String) -> Unit,
    onEditWorkout: (String) -> Unit,
    isActionInProgress: Boolean
) {
    Text(
        text = group.month,
        style = Caption.copy(
            color = TextSecondary,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.padding(start = 20.dp, bottom = 12.dp)
    )

    group.days.forEach { day ->
        DayRow(
            day = day,
            isMenuExpanded = expandedMenuEventId == day.event.id,
            onMenuExpanded = { onMenuExpanded(day.event.id) },
            onMenuDismiss = onMenuDismiss,
            onLeaveWorkout = { onLeaveWorkout(day.event.id) },
            onEditWorkout = { onEditWorkout(day.event.id) },
            isActionInProgress = isActionInProgress
        )
        Spacer(Modifier.height(16.dp))
    }

    Spacer(Modifier.height(8.dp))
}

@Composable
private fun DayRow(
    day: ScheduledDay,
    isMenuExpanded: Boolean,
    onMenuExpanded: () -> Unit,
    onMenuDismiss: () -> Unit,
    onLeaveWorkout: () -> Unit,
    onEditWorkout: () -> Unit,
    isActionInProgress: Boolean
) {
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
                style = Caption.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
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
                        style = LabelLarge.copy(color = CardWhite, fontSize = 15.sp)
                    )
                }
            } else {
                Text(
                    text = "${day.dayNumber}",
                    style = LabelLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        EventCard(
            event = day.event,
            modifier = Modifier.weight(1f),
            isMenuExpanded = isMenuExpanded,
            onMenuExpanded = onMenuExpanded,
            onMenuDismiss = onMenuDismiss,
            onLeaveWorkout = onLeaveWorkout,
            onEditWorkout = onEditWorkout,
            isActionInProgress = isActionInProgress
        )
    }
}

@Composable
private fun EventCard(
    event: WorkoutEvent,
    modifier: Modifier = Modifier,
    isMenuExpanded: Boolean,
    onMenuExpanded: () -> Unit,
    onMenuDismiss: () -> Unit,
    onLeaveWorkout: () -> Unit,
    onEditWorkout: () -> Unit,
    isActionInProgress: Boolean
) {
    val context = LocalContext.current

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
                    style = HeadingStyle3.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
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
                            style = LabelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
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
                    style = LabelMedium.copy(color = TextSecondary)
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = if (!event.locationDirectionsUrl.isNullOrBlank()) {
                    Modifier.clickable {
                        openDirections(context, event.locationDirectionsUrl)
                    }
                } else {
                    Modifier
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(3.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.locationDisplayName.ifBlank { event.location },
                        style = LabelSmall.copy(
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (event.locationDisplayAddress.isNotBlank()) {
                        Text(
                            text = event.locationDisplayAddress,
                            style = Caption.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Normal
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = if (!event.locationDirectionsUrl.isNullOrBlank()) PrimaryBlue else TextSecondary,
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (event.attendees.isNotEmpty()) {
                    EventAttendeesSummary(
                        attendees = event.attendees,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }

                Box {
                    IconButton(
                        onClick = onMenuExpanded,
                        enabled = !isActionInProgress,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = "Workout actions",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = onMenuDismiss
                    ) {
                        DropdownMenuItem(
                            text = { Text("Leave workout", color = DestructiveText) },
                            onClick = onLeaveWorkout,
                            enabled = !isActionInProgress
                        )
                        DropdownMenuItem(
                            text = { Text("Edit workout", color = TextPrimary) },
                            onClick = onEditWorkout,
                            enabled = !isActionInProgress
                        )
                    }
                }
            }
        }
    }
}

private fun openDirections(context: android.content.Context, url: String?) {
    if (url.isNullOrBlank()) return

    val uri = Uri.parse(url)
    val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    try {
        context.startActivity(mapsIntent)
    } catch (_: ActivityNotFoundException) {
        try {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(fallbackIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Unable to open maps", Toast.LENGTH_SHORT).show()
        }
    } catch (_: Exception) {
        Toast.makeText(context, "Unable to open maps", Toast.LENGTH_SHORT).show()
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
        Box {
            Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                attendees.take(3).forEach { attendee ->
                    UserAvatar(
                        name = attendee.name,
                        size = 24.dp,
                        userId = attendee.userId,
                        profilePhotoUrl = attendee.profilePhotoUrl.takeIf { it.isNotBlank() },
                        showShadow = false
                    )
                }
            }
        }

        Text(
            text = buildParticipantNamesAnnotated(attendees.map { it.name }),
            style = LabelMedium.copy(color = TextPrimary, fontWeight = FontWeight.Light),
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
            style = HeadingStyle3.copy(color = TextPrimary)
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Retry", color = CardWhite, fontWeight = FontWeight.SemiBold)
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
            style = HeadingStyle3.copy(color = TextPrimary)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Add a workout to see it here.",
            style = LabelMedium.copy(color = TextSecondary)
        )
    }
}