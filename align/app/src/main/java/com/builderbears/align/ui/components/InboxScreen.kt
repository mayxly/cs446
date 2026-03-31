package com.builderbears.align.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.builderbears.align.ui.components.UserAvatar
import com.builderbears.align.ui.theme.AvatarOrange
import com.builderbears.align.ui.theme.AvatarPink
import com.builderbears.align.ui.theme.AvatarYellow
import com.builderbears.align.ui.theme.AvatarGreen2
import com.builderbears.align.ui.theme.BorderLight
import com.builderbears.align.ui.theme.CardWhite
import com.builderbears.align.ui.theme.DisplayStyle
import com.builderbears.align.ui.theme.ErrorRed
import com.builderbears.align.ui.theme.GradientBlue
import com.builderbears.align.ui.theme.GradientMint
import com.builderbears.align.ui.theme.GradientPink
import com.builderbears.align.ui.theme.GradientYellow
import com.builderbears.align.ui.theme.Indigo
import com.builderbears.align.ui.theme.InboxItemBackground
import com.builderbears.align.ui.theme.Micro
import com.builderbears.align.ui.theme.NotificationAccent
import com.builderbears.align.ui.theme.TextMuted
import com.builderbears.align.ui.theme.TextPrimary
import com.builderbears.align.ui.theme.TextSecondary

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val userInitials: String = "",
    val avatarColor: Color = NotificationAccent,
    val actions: List<NotificationAction> = emptyList(),
    var read: Boolean = false
)

data class NotificationAction(
    val label: String,
    val isPrimary: Boolean = true,
    val onClick: () -> Unit = {}
)

@Composable
fun InboxScreen(
    onDismiss: () -> Unit,
    notifications: List<Notification> = getDefaultNotifications(),
    onNotificationClick: (String, Boolean) -> Unit = { _, _ -> }
) {
    var notificationsList by remember { mutableStateOf(notifications) }
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

                        // Notifications list
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            items(notificationsList.size) { index ->
                                NotificationItem(
                                    notification = notificationsList[index],
                                    onClick = {
                                        val updated = notificationsList.toMutableList()
                                        updated[index] = updated[index].copy(read = true)
                                        notificationsList = updated
                                        onNotificationClick(notificationsList[index].id, true)
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

@Composable
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(InboxItemBackground)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pfp with initials
            if (notification.userInitials.isNotEmpty()) {
                UserAvatar(
                    name = notification.userInitials,
                    size = 40.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NotificationAccent)
                        .align(Alignment.Top)
                        .offset(y = 8.dp)
                )
                Spacer(modifier = Modifier.size(32.dp))
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NotificationAccent)
                    )
                }

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = notification.timestamp,
                    fontSize = 11.sp,
                    color = TextMuted
                )

                // Action buttons
                if (notification.actions.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        notification.actions.forEach { action ->
                            Button(
                                onClick = action.onClick,
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(60.dp),
                                colors = if (action.isPrimary) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = Indigo
                                    )
                                } else {
                                    ButtonDefaults.outlinedButtonColors(
                                        contentColor = TextSecondary
                                    )
                                },
                                border = if (!action.isPrimary) {
                                    BorderStroke(
                                        1.dp,
                                        TextMuted
                                    )
                                } else null,
                                contentPadding = PaddingValues(
                                    horizontal = 8.dp,
                                    vertical = 0.dp
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = action.label,
                                    style = Micro.copy(
                                        color = if (action.isPrimary) CardWhite else TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// TODO: Remove this temp stuff
private fun getDefaultNotifications(): List<Notification> {
    return listOf(
        Notification(
            id = "1",
            title = "Sam Reyes",
            message = "sent you a follow request",
            timestamp = "2 min ago",
            userInitials = "S",
            avatarColor = AvatarPink,
            read = false,
            actions = listOf(
                NotificationAction("Accept", isPrimary = true),
                NotificationAction("Decline", isPrimary = false)
            )
        ),
        Notification(
            id = "2",
            title = "Jordan Park",
            message = "reacted 🔥 to your Morning 5k post",
            timestamp = "18 min ago",
            userInitials = "J",
            avatarColor = AvatarYellow,
            read = false
        ),
        Notification(
            id = "3",
            title = "Alex Kim",
            message = "invited you to Morning Run on Wed, Mar 13",
            timestamp = "1 hr ago",
            userInitials = "A",
            avatarColor = AvatarGreen2,
            read = false,
            actions = listOf(
                NotificationAction("Join", isPrimary = true),
                NotificationAction("Decline", isPrimary = false)
            )
        ),
        Notification(
            id = "4",
            title = "Maya Chen",
            message = "reacted 💪 to your Weight Training post",
            timestamp = "3 hr ago",
            userInitials = "M",
            avatarColor = AvatarOrange,
            read = false
        )
    )
}

// Reusable notification bell button with badge
@Composable
fun NotificationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        NotificationCountBadge(
            count = 3,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
        )
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
