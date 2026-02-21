package com.builderbears.align.ui.screens.addactivity

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AddActivityScreen(viewModel: AddActivityViewModel = viewModel()) {
    Text(text = "Add Activity")
}