package com.builderbears.align.ui.screens.schedule

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = viewModel()) {
    Text(text = "Upcoming")
}