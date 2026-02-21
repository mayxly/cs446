package com.builderbears.align.ui.screens.you

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.builderbears.align.ui.screens.feed.FeedViewModel

@Composable
fun YouScreen(viewModel: YouViewModel = viewModel()) {
    Text(text = "You")
}