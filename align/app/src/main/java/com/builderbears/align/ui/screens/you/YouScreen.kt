package com.builderbears.align.ui.screens.you

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.builderbears.align.data.model.User
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.builderbears.align.data.service.NotificationService
import com.builderbears.align.ui.theme.AvatarGreen
import com.builderbears.align.ui.components.InboxScreen
import com.builderbears.align.ui.components.UserAvatar
import com.builderbears.align.ui.components.NotificationButton
import com.builderbears.align.ui.theme.BorderLight
import com.builderbears.align.ui.theme.BorderMuted
import com.builderbears.align.ui.theme.CardWhite
import com.builderbears.align.ui.theme.DestructiveAction
import com.builderbears.align.ui.theme.DisplayStyle
import com.builderbears.align.ui.theme.GradientBlue
import com.builderbears.align.ui.theme.GradientMint
import com.builderbears.align.ui.theme.GradientPink
import com.builderbears.align.ui.theme.GradientYellow
import com.builderbears.align.ui.theme.HeadingStyle1
import com.builderbears.align.ui.theme.InputBackground
import com.builderbears.align.ui.theme.LabelLarge
import com.builderbears.align.ui.theme.NeutralActionBackground
import com.builderbears.align.ui.theme.NeutralActionText
import com.builderbears.align.ui.theme.PrimaryBlue
import com.builderbears.align.ui.theme.PrimaryBlueLight
import com.builderbears.align.ui.theme.TextMuted
import com.builderbears.align.ui.theme.TextPrimary
import com.builderbears.align.ui.theme.TextSecondary


private val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

@Composable
fun YouScreen(
    onLogout: () -> Unit,
    viewModel: YouViewModel = viewModel(),
    inboxViewModel: InboxViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val unreadCount by inboxViewModel.unreadCount.collectAsState()

    // ViewModel state
    val user by viewModel.user.collectAsState()
    val profilePhotoUrl by viewModel.profilePhotoUrl.collectAsState()
    val isUploadingPhoto by viewModel.isUploadingPhoto.collectAsState()
    val totalWorkouts by viewModel.totalWorkouts.collectAsState()
    val thisMonthWorkouts by viewModel.thisMonthWorkouts.collectAsState()
    val weeklyMinutes by viewModel.weeklyMinutes.collectAsState()
    val topActivity by viewModel.topActivity.collectAsState()
    val pushNotificationsEnabled by viewModel.pushNotificationsEnabled.collectAsState()
    val acceptedFriends by viewModel.friends.collectAsState()
    val friendStatuses by viewModel.friendStatuses.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val passwordSuccess by viewModel.passwordSuccess.collectAsState()

    // Local UI state
    var name by remember(user) { mutableStateOf(user?.name ?: "Your Name") }
    var isEditingName by remember { mutableStateOf(false) }
    var showPasswordFields by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showInbox by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setPushNotificationsEnabled(granted)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.uploadProfilePhoto(it) }
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
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 36.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "You",
                style = DisplayStyle.copy(fontSize = 28.sp, color = TextPrimary)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardWhite,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { showLogoutDialog = true }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Log out",
                            tint = DestructiveAction,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                NotificationButton(onClick = { showInbox = true }, unreadCount = unreadCount)
            }
        }

        HorizontalDivider(
            color = BorderMuted,
            thickness = 1.dp,
            modifier = Modifier
                .padding(start = 20.dp)
                .fillMaxWidth(0.66f)
        )

        Spacer(Modifier.height(20.dp))

        // Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Profile row: avatar | name+handle | workout count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile picture
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploadingPhoto) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(AvatarGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = PrimaryBlue,
                                    strokeWidth = 2.dp
                                )
                            }
                        } else {
                            UserAvatar(
                                name = name,
                                size = 64.dp,
                                userId = user?.userId ?: "",
                                profilePhotoUrl = profilePhotoUrl
                            )
                        }
                        // Camera icon overlay
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 2.dp, y = 2.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.CameraAlt,
                                contentDescription = "Change photo",
                                tint = CardWhite,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    // Name + handle
                    Column(modifier = Modifier.weight(1f)) {
                        if (isEditingName) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BasicTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    textStyle = TextStyle(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = HeadingStyle1.fontFamily,
                                        color = TextPrimary
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    decorationBox = { innerTextField ->
                                        Column {
                                            innerTextField()
                                            Box(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(PrimaryBlue)
                                            )
                                        }
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.updateName(name)
                                        isEditingName = false
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryBlue,
                                        contentColor = CardWhite
                                    ),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        "Save",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = "Edit name",
                                    tint = PrimaryBlue,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { isEditingName = true }
                                )
                            }
                        }
                        Text(
                            text = "@${user?.username ?: "yourhandle"}",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Workout count
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalWorkouts",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Text(
                            text = "WORKOUTS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = BorderLight.copy(alpha = 0.5f), thickness = 1.dp)
                Spacer(Modifier.height(16.dp))

                // Password row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022",
                        fontSize = 16.sp,
                        color = TextPrimary,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { showPasswordFields = !showPasswordFields },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlueLight,
                            contentColor = PrimaryBlue
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            "Change password",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Expandable password fields
                AnimatedVisibility(
                    visible = showPasswordFields,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        PasswordField(
                            label = "Current Password",
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            placeholder = "Enter current password",
                        )
                        Spacer(Modifier.height(12.dp))
                        PasswordField(
                            label = "New Password",
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            placeholder = "Min. 8 characters"
                        )
                        Spacer(Modifier.height(12.dp))
                        PasswordField(
                            label = "Confirm new Password",
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = "Re-enter new password"
                        )
                        if (passwordError != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = passwordError ?: "",
                                fontSize = 13.sp,
                                color = DestructiveAction
                            )
                        }
                        if (passwordSuccess) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Password updated successfully",
                                fontSize = 13.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                            LaunchedEffect(Unit) {
                                delay(1500)
                                showPasswordFields = false
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                                viewModel.clearPasswordState()
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    viewModel.changePassword(currentPassword, newPassword, confirmPassword)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text(
                                    "Update Password",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            IconButton(
                                onClick = {
                                    showPasswordFields = false
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                    viewModel.clearPasswordState()
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "Close",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = BorderLight.copy(alpha = 0.5f), thickness = 1.dp)
                Spacer(Modifier.height(16.dp))

                // Push Notifications row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Push Notifications",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (pushNotificationsEnabled)
                                "Workout invites, reactions, friend requests"
                            else
                                "All push notifications are off",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = pushNotificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.setPushNotificationsEnabled(enabled)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CardWhite,
                            checkedTrackColor = PrimaryBlue,
                            uncheckedThumbColor = CardWhite,
                            uncheckedTrackColor = BorderLight
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Stats Row
        StatsRow(thisMonthWorkouts = thisMonthWorkouts, topActivity = topActivity)

        Spacer(Modifier.height(16.dp))

        // Weekly Activity
        WeeklyActivityCard(weeklyMinutes = weeklyMinutes)

        Spacer(Modifier.height(16.dp))

        // Friends
        FriendsCard(
            friends = acceptedFriends,
            onAddFriendClick = { showAddFriendDialog = true }
        )

        Spacer(Modifier.height(16.dp))

        // Dev Tools
        DevToolsCard(context)

        Spacer(Modifier.height(100.dp))
    }

    if (showInbox) {
        InboxScreen(
            onDismiss = {
                showInbox = false
            },
            inboxViewModel = inboxViewModel
        )
    }

    if (showLogoutDialog) {
        Dialog(onDismissRequest = { showLogoutDialog = false }) {
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
                        text = "Are you sure you want to log out?",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                onLogout()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DestructiveAction)
                        ) {
                            Text("Log Out", color = CardWhite, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { showLogoutDialog = false },
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

    if (showAddFriendDialog) {
        AddFriendDialog(
            searchResults = searchResults,
            friendStatuses = friendStatuses,
            onSearch = { viewModel.searchUsers(it) },
            onRequest = { viewModel.sendFriendRequest(it) },
            onAccept = { viewModel.acceptFriendRequest(it) },
            onCancel = { viewModel.cancelFriendRequest(it) },
            onRemove = { viewModel.removeFriend(it) },
            onDismiss = {
                showAddFriendDialog = false
                viewModel.searchUsers("")
            }
        )
    }
}

@Composable
private fun StatsRow(thisMonthWorkouts: Int, topActivity: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // This Month
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$thisMonthWorkouts",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "THIS MONTH",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Top Activity
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(PrimaryBlueLight, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = YouViewModel.workoutEmoji(topActivity ?: ""),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = YouViewModel.workoutLabel(topActivity ?: ""),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "TOP ACTIVITY",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Top Friend
//        Card(
//            modifier = Modifier.weight(1f).fillMaxHeight(),
//            shape = RoundedCornerShape(14.dp),
//            colors = CardDefaults.cardColors(containerColor = CardWhite),
//            elevation = CardDefaults.cardElevation(0.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .fillMaxHeight()
//                    .padding(vertical = 16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    UserAvatar(
//                        name = "Sam Reyes",
//                        size = 28.dp
//                    )
//                    Spacer(Modifier.width(6.dp))
//                    Text(
//                        text = "Sam Reyes",
//                        fontSize = 12.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = TextPrimary
//                    )
//                }
//                Spacer(Modifier.height(4.dp))
//                Text(
//                    text = "TOP FRIEND",
//                    fontSize = 9.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    color = TextSecondary,
//                    letterSpacing = 0.5.sp
//                )
//            }
//        }
    }
}

@Composable
private fun WeeklyActivityCard(weeklyMinutes: List<Int>) {
    val maxMinutes = weeklyMinutes.maxOrNull()?.coerceAtLeast(1) ?: 1
    val maxBarHeight = 100.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Weekly Activity",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyMinutes.forEachIndexed { index, minutes ->
                    val fraction = minutes.toFloat() / maxMinutes.toFloat()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(maxBarHeight),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Background track
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryBlueLight)
                            )
                            // Active bar
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(maxBarHeight * fraction)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryBlue)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = dayLabels[index],
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsCard(friends: List<User>, onAddFriendClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Friends",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Add friend",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { onAddFriendClick() }
                )
            }

            if (friends.isEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "No friends yet",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            } else {
                Spacer(Modifier.height(16.dp))
                friends.forEach { friend ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            name = friend.name,
                            size = 40.dp,
                            userId = friend.userId,
                            profilePhotoUrl = friend.profilePhotoUrl.takeIf { it.isNotBlank() }
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = friend.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "@${friend.username}",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddFriendDialog(
    searchResults: List<User>,
    friendStatuses: Map<String, String>,
    onSearch: (String) -> Unit,
    onRequest: (String) -> Unit,
    onAccept: (String) -> Unit,
    onCancel: (String) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Search bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InputBackground, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                onSearch(it)
                            },
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = TextPrimary
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search for users",
                                            fontSize = 14.sp,
                                            color = TextMuted.copy(alpha = 0.6f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Results list
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResults, key = { it.userId }) { user ->
                        val status = friendStatuses[user.userId]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                name = user.name,
                                size = 44.dp,
                                userId = user.userId,
                                profilePhotoUrl = user.profilePhotoUrl.takeIf { it.isNotBlank() }
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "@${user.username}",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                            FriendActionButton(
                                status = status,
                                onRequest = { onRequest(user.userId) },
                                onAccept = { onAccept(user.userId) },
                                onCancel = { onCancel(user.userId) },
                                onRemove = { onRemove(user.userId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendActionButton(
    status: String?,
    onRequest: () -> Unit,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    onRemove: () -> Unit
) {
    when (status) {
        "ACCEPTED" -> OutlinedButton(
            onClick = onRemove,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, DestructiveAction),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text("Unfriend", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DestructiveAction)
        }
        "SENT" -> OutlinedButton(
            onClick = onCancel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, BorderLight),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text("Pending", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        }
        "PENDING" -> Button(
            onClick = onAccept,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        else -> Button(
            onClick = onRequest,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text("Request", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DevToolsCard(context: android.content.Context) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { expanded = !expanded }
        ) {
            Text(
                text = "Dev Tools",
                fontSize = 12.sp,
                color = TextMuted
            )
            Icon(
                imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp
                    else Icons.Outlined.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                DevToolButton("Test Friend Request") {
                    NotificationService.showFriendRequestNotification(context)
                }
                Spacer(Modifier.height(6.dp))
                DevToolButton("Test Workout Invite") {
                    NotificationService.showWorkoutInviteNotification(context)
                }
                Spacer(Modifier.height(6.dp))
                DevToolButton("Test Workout Reminder") {
                    NotificationService.showWorkoutReminderNotification(context)
                }
                Spacer(Modifier.height(6.dp))
                DevToolButton("Test App Reminder") {
                    NotificationService.showAppReminderNotification(context)
                }
            }
        }
    }
}

@Composable
private fun DevToolButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryBlueLight,
            contentColor = PrimaryBlue
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = InputBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = LabelLarge.fontFamily,
                        fontWeight = LabelLarge.fontWeight,
                        color = TextPrimary
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}
