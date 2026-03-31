package com.builderbears.align.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.builderbears.align.data.model.AppNotification
import com.builderbears.align.ui.screens.you.InboxViewModel
import com.builderbears.align.ui.theme.CardWhite
import com.builderbears.align.ui.theme.DisplayStyle
import com.builderbears.align.ui.theme.ErrorRed
import com.builderbears.align.ui.theme.GradientBlue
import com.builderbears.align.ui.theme.GradientMint
import com.builderbears.align.ui.theme.GradientPink
import com.builderbears.align.ui.theme.GradientYellow
import com.builderbears.align.ui.theme.InboxItemBackground
import com.builderbears.align.ui.theme.Indigo
import com.builderbears.align.ui.theme.Micro
import com.builderbears.align.ui.theme.NotificationAccent
import com.builderbears.align.ui.theme.TextMuted
import com.builderbears.align.ui.theme.TextPrimary
import com.builderbears.align.ui.theme.TextSecondary

@Composable
fun InboxScreen(
    onDismiss: () -> Unit,
    inboxViewModel: InboxViewModel = viewModel()
) {
    val notifications by inboxViewModel.notifications.collectAsState()
    var isVisible by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = {
            isVisible = false
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable {
                    isVisible = false
                    onDismiss()
                }
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.95f)
                        .clickable(enabled = false) {}
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
                        .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Inbox",
                                style = DisplayStyle.copy(fontSize = 28.sp, color = TextPrimary)
                            )
                            IconButton(onClick = {
                                isVisible = false
                                onDismiss()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        if (notifications.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(
                                    text = "No notifications yet",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                items(notifications, key = { it.id }) { notification ->
                                    NotificationItem(
                                        notification = notification,
                                        onClick = {
                                            if (!notification.read) {
                                                inboxViewModel.markAsRead(notification.id)
                                            }
                                        },
                                        onAccept = {
                                            if (notification.type == "friend_request") {
                                                inboxViewModel.acceptFriendRequest(notification)
                                            }
                                        },
                                        onDecline = {
                                            if (notification.type == "friend_request") {
                                                inboxViewModel.declineFriendRequest(notification)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: AppNotification,
    onClick: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(InboxItemBackground)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UserAvatar(
                name = notification.fromUserName,
                size = 40.dp
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.fromUserName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.read) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NotificationAccent)
                        )
                    }
                }

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatTimestamp(notification.timestamp),
                    fontSize = 11.sp,
                    color = TextMuted
                )

                // Action buttons for friend requests
                if (notification.type == "friend_request" && !notification.read) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.height(28.dp).width(68.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Accept",
                                style = Micro.copy(color = CardWhite, fontWeight = FontWeight.Medium)
                            )
                        }
                        Button(
                            onClick = onDecline,
                            modifier = Modifier.height(28.dp).width(68.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            border = BorderStroke(1.dp, TextMuted),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Decline",
                                style = Micro.copy(color = TextSecondary, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        days < 7 -> "$days days ago"
        else -> "${days / 7}w ago"
    }
}

// Reusable notification bell button with badge
@Composable
fun NotificationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    unreadCount: Int = 0
) {
    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CardWhite,
            shadowElevation = 2.dp,
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onClick)
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

// Notification count badge component (red circle)
@Composable
fun NotificationCountBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(ErrorRed),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            style = Micro.copy(
                color = CardWhite,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
