package com.builderbears.align.ui.screens.feed

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.builderbears.align.data.model.Activity
import com.builderbears.align.data.model.ActivityParticipant
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
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: FeedViewModel = viewModel()) {
    val activities by viewModel.activities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val uploadingActivityIds by viewModel.uploadingActivityIds.collectAsState()

    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    var showInbox by remember { mutableStateOf(false) }
    var pendingUploadActivityId by remember { mutableStateOf<String?>(null) }
    var expandedPhotoUrl by remember { mutableStateOf<String?>(null) }
    var notesSheetActivityId by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val activityId = pendingUploadActivityId
        if (uri != null && activityId != null) {
            viewModel.uploadActivityPhoto(activityId, uri)
        }
        pendingUploadActivityId = null
    }

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

        HorizontalDivider(color = BorderLight, thickness = 1.dp)

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
                        text = "Error: $error",
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
                    items(activities, key = { it.activityId }) { activity ->
                        ActivityCard(
                            activity = activity,
                            currentUserId = currentUserId,
                            isUploading = activity.activityId in uploadingActivityIds,
                            onReactionClick = { emoji ->
                                val hasReacted = activity.reactions[emoji]?.contains(currentUserId) == true
                                if (hasReacted) {
                                    viewModel.removeReaction(activity.activityId, emoji)
                                } else {
                                    viewModel.addReaction(activity.activityId, emoji)
                                }
                            },
                            onAddPhotoClick = {
                                pendingUploadActivityId = activity.activityId
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onExpandPhoto = { expandedPhotoUrl = it },
                            onDeletePhoto = { photoUrl ->
                                viewModel.deleteActivityPhoto(activity.activityId, photoUrl)
                            },
                            onLeaveActivity = {
                                viewModel.leaveActivity(activity.activityId)
                            },
                            onOpenNotes = {
                                notesSheetActivityId = activity.activityId
                            },
                            onSubmitNote = { note ->
                                viewModel.submitParticipantNote(activity.activityId, note)
                            }
                        )
                    }
                }
            }
        }
    }

    val notesActivity = notesSheetActivityId?.let { selectedId ->
        activities.firstOrNull { it.activityId == selectedId }
    }
    if (notesActivity != null) {
        ActivityNotesBottomSheet(
            activity = notesActivity,
            currentUserId = currentUserId,
            onDismiss = { notesSheetActivityId = null },
            onSubmitNote = { note ->
                viewModel.submitParticipantNote(notesActivity.activityId, note)
            }
        )
    }

    expandedPhotoUrl?.let { photoUrl ->
        ExpandedPhotoDialog(
            photoUrl = photoUrl,
            onDismiss = { expandedPhotoUrl = null }
        )
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
    currentUserId: String,
    isUploading: Boolean,
    onReactionClick: (String) -> Unit,
    onAddPhotoClick: () -> Unit,
    onExpandPhoto: (String) -> Unit,
    onDeletePhoto: (String) -> Unit,
    onLeaveActivity: () -> Unit,
    onOpenNotes: () -> Unit,
    onSubmitNote: (String) -> Unit
) {
    var showLeaveMenu by remember { mutableStateOf(false) }
    var showLeaveConfirm by remember { mutableStateOf(false) }
    var pendingDeletePhotoUrl by remember { mutableStateOf<String?>(null) }

    val isParticipant = currentUserId.isNotBlank() && activity.participantIds.contains(currentUserId)
    val noteCount = activity.participantNotes.values.count { it.isNotBlank() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
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

                Column(horizontalAlignment = Alignment.End) {
                    if (isParticipant) {
                        Box {
                            IconButton(
                                onClick = { showLeaveMenu = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Activity actions",
                                    tint = TextSecondary
                                )
                            }
                            DropdownMenu(
                                expanded = showLeaveMenu,
                                onDismissRequest = { showLeaveMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Leave activity") },
                                    onClick = {
                                        showLeaveMenu = false
                                        showLeaveConfirm = true
                                    }
                                )
                            }
                        }
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

            ActivityPhotoSection(
                imageUrls = activity.imageUrls,
                isParticipant = isParticipant,
                isUploading = isUploading,
                onAddPhotoClick = onAddPhotoClick,
                onExpandPhoto = onExpandPhoto,
                onDeletePhotoRequest = { photoUrl -> pendingDeletePhotoUrl = photoUrl }
            )

            Spacer(Modifier.height(12.dp))

            EmojiReactionBar(
                reactions = activity.reactions,
                defaultReactions = defaultReactions,
                onReactionClick = onReactionClick
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Notes (${formatNotesCount(noteCount)})",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Indigo,
                modifier = Modifier.clickable(onClick = onOpenNotes)
            )
        }
    }

    if (showLeaveConfirm) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirm = false },
            title = { Text("Leave activity?") },
            text = { Text("You will no longer see this activity in your feed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveConfirm = false
                        onLeaveActivity()
                    }
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    pendingDeletePhotoUrl?.let { photoUrl ->
        AlertDialog(
            onDismissRequest = { pendingDeletePhotoUrl = null },
            title = { Text("Delete photo?") },
            text = { Text("This will remove the photo for all participants in this activity.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeletePhotoUrl = null
                        onDeletePhoto(photoUrl)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePhotoUrl = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ActivityMembers(
    participants: List<ActivityParticipant>,
    modifier: Modifier = Modifier
) {
    if (participants.isEmpty()) return
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
            participants.take(3).forEach { participant ->
                UserAvatar(
                    name = participant.name,
                    size = 20.dp,
                    userId = participant.userId,
                    profilePhotoUrl = participant.profilePhotoUrl.takeIf { it.isNotBlank() },
                    showShadow = false
                )
            }
        }
        Text(
            text = "with",
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Normal
        )
        val displayNames = participants.take(2).map { it.name }.joinToString(", ")
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
private fun ActivityPhotoSection(
    imageUrls: List<String>,
    isParticipant: Boolean,
    isUploading: Boolean,
    onAddPhotoClick: () -> Unit,
    onExpandPhoto: (String) -> Unit,
    onDeletePhotoRequest: (String) -> Unit
) {
    if (imageUrls.isEmpty() && !isParticipant) return

    Spacer(Modifier.height(8.dp))

    if (imageUrls.isEmpty()) {
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0EEFF)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Indigo, modifier = Modifier.size(28.dp))
            }
        } else {
            AddPhotoZone(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                onClick = onAddPhotoClick
            )
        }
        return
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(imageUrls, key = { it }) { url ->
            ActivityPhotoTile(
                photoUrl = url,
                canDelete = isParticipant,
                onExpandPhoto = onExpandPhoto,
                onDeletePhotoRequest = onDeletePhotoRequest
            )
        }
        if (isParticipant) {
            item {
                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .height(160.dp)
                            .width(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0EEFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Indigo, modifier = Modifier.size(24.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .height(160.dp)
                            .width(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0EEFF))
                            .clickable(onClick = onAddPhotoClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.CameraAlt,
                            contentDescription = "Add photo",
                            tint = Indigo,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityPhotoTile(
    photoUrl: String,
    canDelete: Boolean,
    onExpandPhoto: (String) -> Unit,
    onDeletePhotoRequest: (String) -> Unit
) {
    var showOverlay by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .height(160.dp)
            .width(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { showOverlay = !showOverlay }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (showOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { showOverlay = false }
            )
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PhotoOverlayIconButton(
                    icon = Icons.Outlined.OpenInFull,
                    contentDescription = "Expand photo",
                    onClick = {
                        showOverlay = false
                        onExpandPhoto(photoUrl)
                    }
                )
                PhotoOverlayIconButton(
                    icon = Icons.Outlined.Delete,
                    contentDescription = "Delete photo",
                    enabled = canDelete,
                    onClick = {
                        showOverlay = false
                        if (canDelete) {
                            onDeletePhotoRequest(photoUrl)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PhotoOverlayIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (enabled) 0.95f else 0.45f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) TextPrimary else TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AddPhotoZone(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0EEFF))
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFF6B7FD4).copy(alpha = 0.5f),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Outlined.CameraAlt,
                contentDescription = "Add a photo",
                tint = Indigo,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Add a photo",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Indigo
            )
            Text(
                text = "Tap to upload from your library",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun EmojiReactionBar(
    reactions: Map<String, List<String>>,
    defaultReactions: List<String>,
    onReactionClick: (String) -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityNotesBottomSheet(
    activity: Activity,
    currentUserId: String,
    onDismiss: () -> Unit,
    onSubmitNote: (String) -> Unit
) {
    val isParticipant = activity.participantIds.contains(currentUserId)
    val hasSubmittedNote = !activity.participantNotes[currentUserId].isNullOrBlank()
    var noteDraft by remember(activity.activityId) { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Individual Notes",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            activity.participants.forEach { participant ->
                ParticipantNoteRow(
                    participant = participant,
                    note = activity.participantNotes[participant.userId].orEmpty()
                )
            }

            if (isParticipant && !hasSubmittedNote) {
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { noteDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Add your note") },
                    placeholder = { Text("Share your workout note") },
                    minLines = 2,
                    maxLines = 4
                )
                Button(
                    onClick = {
                        val trimmed = noteDraft.trim()
                        if (trimmed.isNotEmpty()) {
                            onSubmitNote(trimmed)
                            noteDraft = ""
                        }
                    },
                    enabled = noteDraft.trim().isNotEmpty(),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Send")
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ParticipantNoteRow(
    participant: ActivityParticipant,
    note: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        UserAvatar(
            name = participant.name,
            size = 24.dp,
            userId = participant.userId,
            profilePhotoUrl = participant.profilePhotoUrl.takeIf { it.isNotBlank() },
            showShadow = false
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = participant.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = note.ifBlank { "No note yet" },
                fontSize = 14.sp,
                color = if (note.isBlank()) TextMuted else TextSecondary
            )
        }
    }
}

@Composable
private fun ExpandedPhotoDialog(
    photoUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

private fun formatNotesCount(count: Int): String {
    return if (count > 10) "10+" else count.toString()
}
