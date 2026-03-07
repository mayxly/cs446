package com.builderbears.align.ui.screens.addactivity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.builderbears.align.ui.theme.GradientBlue
import com.builderbears.align.ui.theme.GradientMint
import com.builderbears.align.ui.theme.GradientPink
import com.builderbears.align.ui.theme.GradientYellow
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val PrimaryBlue   = Color(0xFF4353E8)
private val LightBlue     = Color(0xFFEEF0FD)
private val CardWhite     = Color(0xFFFFFFFF)
private val TextPrimary   = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF9E9E9E)
private val BorderColor   = Color(0xFFE0E0E0)
private val SelectedBorder = Color(0xFF4353E8)

data class Friend(val id: Int, val name: String, val handle: String, val initials: String, val color: Color)

private val workoutTypes = listOf(
    "Run" to "🏃", "Gym" to "🏋️", "Yoga" to "🧘", "Cycle" to "🚴",
    "Swim" to "🏊", "Basketball" to "🏀", "HIIT" to "🔥", "Other" to "✨"
)

private val friends = listOf(
    Friend(1, "Alex Kim",    "@alexkim",  "A", Color(0xFF4CAF50)),
    Friend(2, "Sam Reyes",   "@samreyes", "S", Color(0xFFE91E63)),
    Friend(3, "Jorden Park", "@jorrp",    "J", Color(0xFFFFD700)),
    Friend(4, "Maya Chen",   "@mayac",    "M", Color(0xFFFF8A65))
)

@Composable
fun AddActivityScreen(viewModel: AddActivityViewModel = viewModel()) {
    var activityName   by remember { mutableStateOf("") }
    var selectedDate   by remember { mutableStateOf<LocalDate?>(null) }
    var selectedHour   by remember { mutableStateOf(2) }
    var selectedMinute by remember { mutableStateOf(10) }
    var isPm           by remember { mutableStateOf(true) }
    var location       by remember { mutableStateOf("") }
    var workoutType    by remember { mutableStateOf("Run") }
    var description    by remember { mutableStateOf("") }
    var invitedFriends by remember { mutableStateOf(setOf(2, 3)) }

    var showCalendar   by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

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
        Text(
            text = "Add Activity",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 16.dp)
        )

        HorizontalDivider(color = BorderColor, thickness = 1.dp)

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
        InputCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null,
                    tint = TextSecondary, modifier = Modifier.padding(end = 8.dp))
                TextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("e.g. Riverside Park", color = TextSecondary) },
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
            friends.forEach { friend ->
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

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { /* implement later */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Create Workout", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(100.dp))
    }
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
                                color = if (isSelected) SelectedBorder else Color.Transparent,
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
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) SelectedBorder else Color.Transparent,
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
                    .border(1.5.dp, if (isSelected) PrimaryBlue else BorderColor, CircleShape),
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