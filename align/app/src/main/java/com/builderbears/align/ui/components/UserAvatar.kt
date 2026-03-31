package com.builderbears.align.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.builderbears.align.ui.utils.userColorForId

@Composable
fun UserAvatar(
    name: String,
    size: Dp,
    userId: String = "",
    profilePhotoUrl: String? = null,
    showShadow: Boolean = true
) {
    val color = userColorForId(userId)
    val fontSize = (size.value * 0.4f).sp
    val fontWeight = if (size >= 28.dp) FontWeight.Bold else FontWeight.SemiBold
    val borderWidth = (size.value * 0.06f).dp
    val shadowModifier = if (showShadow) {
        Modifier.shadow(elevation = 2.dp, shape = CircleShape, clip = false)
    } else {
        Modifier
    }

    val totalSize = size + borderWidth * 2

    Box(
        modifier = Modifier
            .size(totalSize)
            .then(shadowModifier)
            .clip(CircleShape)
            .background(Color.White)
            .padding(borderWidth),
        contentAlignment = Alignment.Center
    ) {
        if (!profilePhotoUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(profilePhotoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile photo",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = fontSize
                )
            }
        }
    }
}
