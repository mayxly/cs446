package com.builderbears.align.ui.screens.addactivity

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.builderbears.align.data.model.User
import com.builderbears.align.ui.navigation.Route
import com.builderbears.align.ui.components.InboxScreen
import com.builderbears.align.ui.components.NotificationButton
import com.builderbears.align.ui.theme.BorderLight
import com.builderbears.align.ui.theme.CardWhite
import com.builderbears.align.ui.theme.ErrorRed
import com.builderbears.align.ui.theme.GradientBlue
import com.builderbears.align.ui.theme.GradientMint
import com.builderbears.align.ui.theme.GradientPink
import com.builderbears.align.ui.theme.GradientYellow
import com.builderbears.align.ui.theme.LightBlue
import com.builderbears.align.ui.theme.PrimaryBlue
import com.builderbears.align.ui.theme.TextPrimary
import com.builderbears.align.ui.theme.TextSecondary
import com.builderbears.align.ui.utils.userColorForId
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale


data class Friend(val id: String, val name: String, val handle: String, val initials: String, val color: Color)

private val workoutTypes = listOf(
    "Run" to "🏃", "Gym" to "🏋️", "Yoga" to "🧘", "Cycle" to "🚴",
    "Swim" to "🏊", "Basketball" to "🏀", "HIIT" to "🔥", "Other" to "✨"
)

@Composable
fun AddActivityScreen(
    navController: NavController? = null,
    activityId: String? = null,
    isModal: Boolean = false,
    onDismissRequest: (() -> Unit)? = null,
    viewModel: AddActivityViewModel = viewModel()
) {
    val context = LocalContext.current
    val isEditMode = !activityId.isNullOrBlank()

    var activityName   by remember { mutableStateOf("") }
    var selectedDate   by remember { mutableStateOf<LocalDate?>(null) }
    var selectedHour   by remember { mutableStateOf(2) }
    var selectedMinute by remember { mutableStateOf(10) }
    var isPm           by remember { mutableStateOf(true) }
    var location       by remember { mutableStateOf("") }
    var selectedLocationPlaceId by remember { mutableStateOf("") }
    var selectedLocationLat by remember { mutableStateOf<Double?>(null) }
    var selectedLocationLng by remember { mutableStateOf<Double?>(null) }
    var lastResolvedPlaceId by remember { mutableStateOf("") }
    var lastResolvedLocation by remember { mutableStateOf("") }
    var lastResolvedDisplayName by remember { mutableStateOf("") }
    var lastResolvedDisplayAddress by remember { mutableStateOf("") }
    var lastResolvedLat by remember { mutableStateOf<Double?>(null) }
    var lastResolvedLng by remember { mutableStateOf<Double?>(null) }
    var selectedLocationDisplayName by remember { mutableStateOf("") }
    var selectedLocationDisplayAddress by remember { mutableStateOf("") }
    var selectedLocationLabel by remember { mutableStateOf("") }
    var selectedLocationFullText by remember { mutableStateOf("") }
    var workoutType    by remember { mutableStateOf("Run") }
    var description    by remember { mutableStateOf("") }
    var invitedFriends by remember { mutableStateOf(setOf<String>() ) }

    var showCalendar   by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showInbox by remember { mutableStateOf(false) }
    var friendSearchQuery by remember { mutableStateOf("") }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val friends = remember(viewModel.availableUsers, currentUserId) {
        viewModel.availableUsers
            .filter { it.userId != currentUserId }
            .map { it.toFriend() }
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(activityId) {
        if (isEditMode) {
            viewModel.loadActivityForEdit(activityId.orEmpty())
        } else {
            viewModel.clearEditingActivity()
        }
    }

    LaunchedEffect(viewModel.editingActivity?.activityId, isEditMode, currentUserId) {
        if (!isEditMode) return@LaunchedEffect

        val activity = viewModel.editingActivity ?: return@LaunchedEffect

        activityName = activity.name
        selectedDate = runCatching { LocalDate.parse(activity.date) }.getOrNull()

        val parsed = parsePickerTime(activity.time)
        selectedHour = parsed.hour
        selectedMinute = parsed.minute
        isPm = parsed.isPm

        location = activity.location
        selectedLocationPlaceId = activity.locationPlaceId
        selectedLocationLat = activity.locationLat
        selectedLocationLng = activity.locationLng
        selectedLocationDisplayName = activity.locationDisplayName
        selectedLocationDisplayAddress = activity.locationDisplayAddress
        selectedLocationLabel = activity.locationDisplayName.ifBlank { activity.location }
        selectedLocationFullText = listOf(
            activity.locationDisplayName,
            activity.locationDisplayAddress
        ).filter { it.isNotBlank() }.joinToString(", ").ifBlank { activity.location }

        lastResolvedPlaceId = activity.locationPlaceId
        lastResolvedLocation = activity.location
        lastResolvedDisplayName = activity.locationDisplayName
        lastResolvedDisplayAddress = activity.locationDisplayAddress
        lastResolvedLat = activity.locationLat
        lastResolvedLng = activity.locationLng

        workoutType = activity.workoutType.ifBlank { "Run" }
        description = activity.description
        invitedFriends = activity.participantIds
            .filter { it.isNotBlank() && it != currentUserId }
            .toSet()
    }

    LaunchedEffect(viewModel.saveSuccess) {
        if (viewModel.saveSuccess) {
            activityName = ""
            selectedDate = null
            selectedHour = 2
            selectedMinute = 10
            isPm = true
            location = ""
            selectedLocationPlaceId = ""
            selectedLocationLat = null
            selectedLocationLng = null
            lastResolvedPlaceId = ""
            lastResolvedLocation = ""
            lastResolvedDisplayName = ""
            lastResolvedDisplayAddress = ""
            lastResolvedLat = null
            lastResolvedLng = null
            selectedLocationDisplayName = ""
            selectedLocationDisplayAddress = ""
            selectedLocationLabel = ""
            selectedLocationFullText = ""
            viewModel.resetLocationSearchSession()
            workoutType = "Run"
            description = ""
            invitedFriends = setOf<String>()
            viewModel.saveSuccess = false
            if (isModal) {
                onDismissRequest?.invoke()
            } else {
                navController?.navigate(Route.Schedule.path) {
                    // Reset to root feed before showing schedule to keep backstack clean.
                    popUpTo(Route.Feed.path) { inclusive = false }
                    launchSingleTop = true
                    restoreState = false
                }
            }
        }
    }

    LaunchedEffect(viewModel.saveError) {
        if (viewModel.saveError != null) {
            Toast.makeText(
                context,
                "Failed to ${if (isEditMode) "update" else "create"} activity: ${viewModel.saveError}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    val scrollState = rememberScrollState()
    var isFriendListInteracting by remember { mutableStateOf(false) }

    if (isEditMode && viewModel.isLoadingEditingActivity) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
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
                .verticalScroll(scrollState, enabled = !isFriendListInteracting)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditMode) "Edit Workout" else "Add Activity",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (isModal) {
                IconButton(onClick = { onDismissRequest?.invoke() }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            } else {
                NotificationButton(onClick = { showInbox = true })
            }
        }

        HorizontalDivider(color = BorderLight, thickness = 1.dp)

        Spacer(Modifier.height(20.dp))

        SectionLabel("NAME")
        InputCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("→", color = TextSecondary, fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                TextField(
                    value = activityName,
                    onValueChange = { activityName = it },
                    placeholder = { Text("e.g. Morning Run", color = TextSecondary) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (viewModel.nameError != null) {
            Text(
                text = viewModel.nameError!!,
                fontSize = 12.sp,
                color = ErrorRed,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp, end = 16.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        SectionLabel("DATE & TIME")

        InputCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable { showCalendar = !showCalendar; showTimePicker = false }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null,
                    tint = TextSecondary, modifier = Modifier.padding(end = 10.dp))
                Column {
                    Text("Date", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = selectedDate?.format(DateTimeFormatter.ofPattern("MMMM d'th', yyyy"))
                            ?: "Select a date",
                        color = if (selectedDate != null) TextPrimary else TextSecondary,
                        fontSize = 15.sp
                    )
                }
            }
        }
        if (viewModel.dateError != null) {
            Text(
                text = viewModel.dateError!!,
                fontSize = 12.sp,
                color = ErrorRed,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp, end = 16.dp)
            )
        }

        AnimatedVisibility(visible = showCalendar, enter = expandVertically(), exit = shrinkVertically()) {
            CalendarPicker(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it; showCalendar = false },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        InputCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable { showTimePicker = !showTimePicker; showCalendar = false }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule, contentDescription = null,
                    tint = TextSecondary, modifier = Modifier.padding(end = 10.dp))
                Column {
                    Text("Time", fontSize = 11.sp, color = TextSecondary)
                    val timeStr = if (showTimePicker || selectedDate != null)
                        "$selectedHour:${selectedMinute.toString().padStart(2,'0')} ${if(isPm) "PM" else "AM"}"
                    else "Select a time"
                    Text(
                        text = timeStr,
                        color = if (selectedDate != null) TextPrimary else TextSecondary,
                        fontSize = 15.sp
                    )
                }
            }
        }
        if (viewModel.timeError != null) {
            Text(
                text = viewModel.timeError!!,
                fontSize = 12.sp,
                color = ErrorRed,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp, end = 16.dp)
            )
        }

        AnimatedVisibility(visible = showTimePicker, enter = expandVertically(), exit = shrinkVertically()) {
            TimePickerWheel(
                hour = selectedHour,
                minute = selectedMinute,
                isPm = isPm,
                onHourChange = { selectedHour = it },
                onMinuteChange = { selectedMinute = it },
                onAmPmChange = { isPm = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        SectionLabel("LOCATION")
        val locationSuggestions = viewModel.locationSuggestions

        InputCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null,
                    tint = TextSecondary, modifier = Modifier.padding(end = 8.dp))
                TextField(
                    value = location,
                    onValueChange = { updated ->
                        location = updated
                        if (updated != selectedLocationLabel) {
                            selectedLocationPlaceId = ""
                            selectedLocationLat = null
                            selectedLocationLng = null
                            selectedLocationDisplayName = ""
                            selectedLocationDisplayAddress = ""
                            selectedLocationLabel = ""
                            selectedLocationFullText = ""
                            viewModel.resetLocationSearchSession(clearSuggestions = false)
                        }
                        if (updated.isBlank()) {
                            viewModel.resetLocationSearchSession()
                        } else {
                            viewModel.onLocationInputChanged(
                                context = context,
                                query = updated,
                                selectedLocationLabel = selectedLocationLabel,
                                selectedLocationPlaceId = selectedLocationPlaceId
                            )
                        }
                    },
                    placeholder = { Text("Enter a location", color = TextSecondary) },
                    singleLine = true,
                    maxLines = 1,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (viewModel.locationConfigError != null) {
            Text(
                text = viewModel.locationConfigError ?: "Missing MAPS_API_KEY configuration",
                fontSize = 12.sp,
                color = ErrorRed,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp, end = 16.dp)
            )
        }
        if (locationSuggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                ThresholdScrollableList(
                    items = locationSuggestions,
                    maxVisibleItems = 4,
                    rowHeight = 58.dp,
                    rowSpacing = 0.dp,
                    keySelector = { it.placeId },
                    modifier = Modifier.fillMaxWidth()
                ) { index, suggestion ->
                    if (index > 0) {
                        HorizontalDivider(color = BorderLight, thickness = 0.7.dp)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                location = suggestion.primaryText
                                selectedLocationDisplayName = suggestion.primaryText
                                selectedLocationDisplayAddress = parseDisplayAddress(
                                    fullText = suggestion.fullText,
                                    primaryText = suggestion.primaryText
                                )
                                selectedLocationLabel = suggestion.primaryText
                                selectedLocationFullText = suggestion.fullText
                                selectedLocationPlaceId = suggestion.placeId
                                selectedLocationLat = null
                                selectedLocationLng = null
                                viewModel.hideLocationSuggestions()
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = suggestion.primaryText,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = suggestion.fullText,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
        if (viewModel.locationError != null) {
            Text(
                text = viewModel.locationError!!,
                fontSize = 12.sp,
                color = ErrorRed,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp, end = 16.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        SectionLabel("WORKOUT TYPE")
        WorkoutTypeGrid(
            selected = workoutType,
            onSelect = { workoutType = it },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(20.dp))

        Row(modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)) {
            Text("DESCRIPTION", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp, color = TextSecondary)
            Text(" · optional", fontSize = 11.sp, color = TextSecondary)
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(120.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            TextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Add notes, goals, equipment...", color = TextSecondary) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(20.dp))

        SectionLabel("INVITE FRIENDS")
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    TextField(
                        value = friendSearchQuery,
                        onValueChange = { friendSearchQuery = it },
                        placeholder = { Text("Search for friends", color = TextSecondary, fontSize = 14.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor   = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor   = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            val filteredFriends = remember(friends, friendSearchQuery) {
                if (friendSearchQuery.isBlank()) friends
                else friends.filter { friend ->
                    friend.name.contains(friendSearchQuery, ignoreCase = true) ||
                    friend.handle.contains(friendSearchQuery, ignoreCase = true)
                }
            }
            val maxVisibleFriends = 4
            val friendRowHeight = 68.dp
            val friendRowSpacing = 8.dp

            when {
                viewModel.isLoadingUsers -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryBlue
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Loading friends...", color = TextSecondary, fontSize = 13.sp)
                    }
                }

                viewModel.usersLoadError != null -> {
                    Text(
                        text = viewModel.usersLoadError ?: "Failed to load friends",
                        fontSize = 12.sp,
                        color = ErrorRed,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Button(
                        onClick = { viewModel.loadUsers() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Retry", fontSize = 13.sp)
                    }
                }

                friends.isEmpty() -> {
                    Text(
                        text = "No friends found. Ask others to create accounts first.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                else -> {
                    if (filteredFriends.isEmpty()) {
                        Text(
                            text = "No friends match \"$friendSearchQuery\".",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else {
                        ThresholdScrollableList(
                            items = filteredFriends,
                            maxVisibleItems = maxVisibleFriends,
                            rowHeight = friendRowHeight,
                            rowSpacing = friendRowSpacing,
                            keySelector = { it.id },
                            modifier = Modifier.fillMaxWidth(),
                            onScrollInteractionChanged = { isFriendListInteracting = it }
                        ) { _, friend ->
                            val isSelected = invitedFriends.contains(friend.id)
                            FriendRow(
                                friend = friend,
                                isSelected = isSelected,
                                onClick = {
                                    invitedFriends = if (isSelected) invitedFriends - friend.id
                                    else invitedFriends + friend.id
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (viewModel.validateFields(
                    name = activityName,
                    selectedDate = selectedDate,
                    selectedHour = selectedHour,
                    selectedMinute = selectedMinute,
                    isPm = isPm,
                    location = location,
                    locationPlaceId = selectedLocationPlaceId
                )) {
                    coroutineScope.launch {
                        var resolvedLocation = location
                        var resolvedPlaceId = selectedLocationPlaceId
                        var resolvedLat = selectedLocationLat
                        var resolvedLng = selectedLocationLng
                        var resolvedDisplayName = selectedLocationDisplayName
                        var resolvedDisplayAddress = selectedLocationDisplayAddress

                        val shouldFetchPlace = selectedLocationPlaceId.isNotBlank() &&
                            selectedLocationPlaceId != lastResolvedPlaceId

                        if (shouldFetchPlace) {
                            val resolvedResult = viewModel.resolveLocationForSubmit(
                                context = context,
                                selectedLocationPlaceId = selectedLocationPlaceId,
                                selectedLocationDisplayName = selectedLocationDisplayName.ifBlank { selectedLocationLabel },
                                selectedLocationFullText = selectedLocationFullText,
                                fallbackLocation = location
                            )

                            if (resolvedResult.isFailure) {
                                Toast.makeText(
                                    context,
                                    resolvedResult.exceptionOrNull()?.message ?: "Unable to resolve selected location",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }

                            val resolved = resolvedResult.getOrNull()
                            if (resolved != null) {
                                resolvedLocation = resolved.location
                                resolvedPlaceId = resolved.placeId
                                resolvedLat = resolved.lat
                                resolvedLng = resolved.lng
                                resolvedDisplayName = resolved.displayName
                                resolvedDisplayAddress = resolved.displayAddress

                                selectedLocationLat = resolvedLat
                                selectedLocationLng = resolvedLng
                                selectedLocationPlaceId = resolvedPlaceId
                                selectedLocationDisplayName = resolvedDisplayName
                                selectedLocationDisplayAddress = resolvedDisplayAddress

                                lastResolvedPlaceId = resolvedPlaceId
                                lastResolvedLocation = resolvedLocation
                                lastResolvedDisplayName = resolvedDisplayName
                                lastResolvedDisplayAddress = resolvedDisplayAddress
                                lastResolvedLat = resolvedLat
                                lastResolvedLng = resolvedLng
                            }
                        } else if (selectedLocationPlaceId == lastResolvedPlaceId && lastResolvedPlaceId.isNotBlank()) {
                            resolvedLocation = lastResolvedLocation.ifBlank { selectedLocationFullText.ifBlank { location } }
                            resolvedPlaceId = lastResolvedPlaceId
                            resolvedDisplayName = lastResolvedDisplayName.ifBlank { selectedLocationDisplayName }
                            resolvedDisplayAddress = lastResolvedDisplayAddress
                            resolvedLat = lastResolvedLat
                            resolvedLng = lastResolvedLng
                            selectedLocationLat = resolvedLat
                            selectedLocationLng = resolvedLng
                            selectedLocationDisplayName = resolvedDisplayName
                            selectedLocationDisplayAddress = resolvedDisplayAddress
                        }
                        viewModel.resetLocationSearchSession()

                        if (isEditMode) {
                            viewModel.updateActivity(
                                activityId = activityId.orEmpty(),
                                name = activityName,
                                description = description,
                                workoutType = workoutType,
                                location = resolvedLocation,
                                locationDisplayName = resolvedDisplayName,
                                locationDisplayAddress = resolvedDisplayAddress,
                                locationPlaceId = resolvedPlaceId,
                                locationLat = resolvedLat,
                                locationLng = resolvedLng,
                                date = selectedDate?.toString() ?: "",
                                time = "$selectedHour:${selectedMinute.toString().padStart(2, '0')} ${if (isPm) "PM" else "AM"}",
                                invitedUserIds = invitedFriends.toList()
                            )
                        } else {
                            viewModel.saveActivity(
                                name = activityName,
                                description = description,
                                workoutType = workoutType,
                                location = resolvedLocation,
                                locationDisplayName = resolvedDisplayName,
                                locationDisplayAddress = resolvedDisplayAddress,
                                locationPlaceId = resolvedPlaceId,
                                locationLat = resolvedLat,
                                locationLng = resolvedLng,
                                date = selectedDate?.toString() ?: "",
                                time = "$selectedHour:${selectedMinute.toString().padStart(2, '0')} ${if (isPm) "PM" else "AM"}",
                                invitedUserIds = invitedFriends.toList()
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            enabled = !viewModel.isSaving && !viewModel.isResolvingLocation,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                if (viewModel.isResolvingLocation) {
                    "Resolving location..."
                } else if (isEditMode) {
                    "Save Workout"
                } else {
                    "Create Workout"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(100.dp))
    }

    if (showInbox) {
        InboxScreen(
            onDismiss = { showInbox = false }
        )
    }
}

private data class PickerTime(
    val hour: Int,
    val minute: Int,
    val isPm: Boolean
)

private fun parsePickerTime(raw: String): PickerTime {
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    val parsed = runCatching {
        LocalTime.parse(raw.trim().uppercase(Locale.US), formatter)
    }.getOrNull() ?: return PickerTime(hour = 2, minute = 10, isPm = true)

    val hour12 = when {
        parsed.hour == 0 -> 12
        parsed.hour > 12 -> parsed.hour - 12
        else -> parsed.hour
    }

    return PickerTime(
        hour = hour12,
        minute = parsed.minute,
        isPm = parsed.hour >= 12
    )
}

@Composable
private fun <T> ThresholdScrollableList(
    items: List<T>,
    maxVisibleItems: Int,
    rowHeight: Dp,
    rowSpacing: Dp,
    keySelector: (T) -> Any,
    modifier: Modifier = Modifier,
    onScrollInteractionChanged: ((Boolean) -> Unit)? = null,
    itemContent: @Composable (index: Int, item: T) -> Unit
) {
    if (items.isEmpty()) return

    val scrollable = items.size > maxVisibleItems
    if (!scrollable) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(rowSpacing)
        ) {
            items.forEachIndexed { index, item ->
                itemContent(index, item)
            }
        }
        return
    }

    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val maxListHeight = (rowHeight * maxVisibleItems) + (rowSpacing * (maxVisibleItems - 1))
    val itemHeightPx = with(density) { (rowHeight + rowSpacing).toPx() }
    val viewportHeightPx = with(density) { maxListHeight.toPx() }
    val spacingPx = with(density) { rowSpacing.toPx() }
    val totalContentHeightPx = (itemHeightPx * items.size) - spacingPx
    val maxScrollPx = (totalContentHeightPx - viewportHeightPx).coerceAtLeast(1f)
    val estimatedScrollPx = (listState.firstVisibleItemIndex * itemHeightPx) + listState.firstVisibleItemScrollOffset
    val scrollProgress = (estimatedScrollPx / maxScrollPx).coerceIn(0f, 1f)
    val thumbHeightPx = ((viewportHeightPx / totalContentHeightPx) * viewportHeightPx)
        .coerceIn(with(density) { 24.dp.toPx() }, viewportHeightPx)
    val thumbOffsetPx = (scrollProgress * (viewportHeightPx - thumbHeightPx)).coerceIn(0f, viewportHeightPx - thumbHeightPx)
    val thumbHeightDp = with(density) { thumbHeightPx.toDp() }
    val thumbOffsetDp = with(density) { thumbOffsetPx.toDp() }

    Box(
        modifier = modifier.height(maxListHeight)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 10.dp)
                .pointerInput(items.size) {
                    detectVerticalDragGestures(
                        onDragStart = { onScrollInteractionChanged?.invoke(true) },
                        onVerticalDrag = { _, _ -> },
                        onDragEnd = { onScrollInteractionChanged?.invoke(false) },
                        onDragCancel = { onScrollInteractionChanged?.invoke(false) }
                    )
                },
            verticalArrangement = Arrangement.spacedBy(rowSpacing)
        ) {
            itemsIndexed(items, key = { _, item -> keySelector(item) }) { index, item ->
                itemContent(index, item)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 2.dp)
                .width(4.dp)
                .fillMaxSize()
                .clip(RoundedCornerShape(99.dp))
                .background(PrimaryBlue.copy(alpha = 0.12f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 2.dp, top = thumbOffsetDp)
                .width(4.dp)
                .height(thumbHeightDp)
                .clip(RoundedCornerShape(99.dp))
                .background(PrimaryBlue.copy(alpha = 0.9f))
        )
    }
}

private fun parseDisplayAddress(fullText: String, primaryText: String): String {
    val normalizedFull = fullText.trim()
    if (normalizedFull.isEmpty()) return ""

    val normalizedPrimary = primaryText.trim()
    if (normalizedPrimary.isEmpty()) return normalizedFull

    return normalizedFull
        .removePrefix(normalizedPrimary)
        .removePrefix(",")
        .trim()
}

private fun User.toFriend(): Friend {
    val normalizedName = name.ifBlank { email.ifBlank { "User" } }
    val initials = normalizedName
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "U" }

    val handle = if (email.isNotBlank()) email else "@${normalizedName.lowercase().replace(" ", "")}" 

    return Friend(
        id = userId,
        name = normalizedName,
        handle = handle,
        initials = initials,
        color = userColorForId(userId)
    )
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        color = TextSecondary,
        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun InputCard(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun WorkoutTypeGrid(selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        workoutTypes.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (type, emoji) ->
                    val isSelected = type == selected
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) PrimaryBlue else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelect(type) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) LightBlue else CardWhite
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(emoji, fontSize = 22.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(type, fontSize = 12.sp, color = if (isSelected) PrimaryBlue else TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendRow(friend: Friend, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) PrimaryBlue else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF5F6FE) else CardWhite
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(friend.color),
                contentAlignment = Alignment.Center
            ) {
                Text(friend.initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(friend.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                Text(friend.handle, fontSize = 13.sp, color = TextSecondary)
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) PrimaryBlue else Color.Transparent)
                    .border(1.5.dp, if (isSelected) PrimaryBlue else BorderLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun CalendarPicker(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("<",
                    modifier = Modifier.clickable { displayMonth = displayMonth.minusMonths(1) },
                    fontSize = 18.sp, color = TextPrimary)
                Text(
                    displayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    fontWeight = FontWeight.SemiBold, fontSize = 15.sp
                )
                Text(">",
                    modifier = Modifier.clickable { displayMonth = displayMonth.plusMonths(1) },
                    fontSize = 18.sp, color = TextPrimary)
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("S","M","T","W","T","F","S").forEach {
                    Text(it, modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center, fontSize = 12.sp,
                        color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(8.dp))

            val firstDay = displayMonth.atDay(1)
            val startOffset = firstDay.dayOfWeek.value % 7
            val daysInMonth = displayMonth.lengthOfMonth()
            val totalCells = startOffset + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val dayNum = row * 7 + col - startOffset + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNum in 1..daysInMonth) {
                                val date = displayMonth.atDay(dayNum)
                                val isToday = date == today
                                val isSelected = date == selectedDate
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) PrimaryBlue else Color.Transparent)
                                        .border(
                                            width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                            color = if (isToday && !isSelected) PrimaryBlue else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { onDateSelected(date) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        fontSize = 14.sp,
                                        color = when {
                                            isSelected -> Color.White
                                            isToday -> PrimaryBlue
                                            else -> TextPrimary
                                        },
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
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
private fun TimePickerWheel(
    hour: Int, minute: Int, isPm: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onAmPmChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val hours = (1..12).toList()
    val minutes = listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WheelColumn(
                items = hours,
                selected = hour,
                onSelect = onHourChange,
                display = { it.toString() }
            )

            Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp))

            WheelColumn(
                items = minutes,
                selected = minute,
                onSelect = onMinuteChange,
                display = { it.toString().padStart(2, '0') }
            )

            Spacer(Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(false, true).forEach { pm ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPm == pm) PrimaryBlue else Color.Transparent)
                            .clickable { onAmPmChange(pm) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (pm) "PM" else "AM",
                            color = if (isPm == pm) Color.White else TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> WheelColumn(
    items: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    display: (T) -> String
) {
    val selectedIdx = items.indexOf(selected).coerceAtLeast(0)
    val visibleRange = (-2..2)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        visibleRange.forEach { offset ->
            val idx = selectedIdx + offset
            val item = items.getOrNull(((idx % items.size) + items.size) % items.size)
            val isSelected = offset == 0
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) PrimaryBlue else Color.Transparent)
                    .clickable { item?.let { onSelect(it) } },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item?.let { display(it) } ?: "",
                    fontSize = if (isSelected) 18.sp else 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected -> Color.White
                        kotlin.math.abs(offset) == 1 -> TextPrimary.copy(alpha = 0.6f)
                        else -> TextPrimary.copy(alpha = 0.3f)
                    }
                )
            }
        }
    }
}