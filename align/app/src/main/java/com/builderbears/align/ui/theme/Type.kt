package com.builderbears.align.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.builderbears.align.R
import androidx.compose.ui.unit.sp

val Lora = FontFamily(
    Font(R.font.lora_bold, weight = FontWeight.Bold),
    Font(R.font.lora_semibold, weight = FontWeight.SemiBold)
)

val Manrope = FontFamily(
    Font(R.font.manrope_bold, weight = FontWeight.Bold),
    Font(R.font.manrope_semibold, weight = FontWeight.SemiBold)
)

val DisplayStyle = TextStyle(
    fontFamily = Lora,
    fontWeight = FontWeight.Bold,
    fontSize = 30.sp,
    lineHeight = (30 * 1.15).sp
)

val HeadingStyle1 = TextStyle(
    fontFamily = Lora,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = (20 * 1.25).sp
)

val HeadingStyle2 = TextStyle(
    fontFamily = Lora,
    fontWeight = FontWeight.SemiBold,
    fontSize = 17.sp,
    lineHeight = (17 * 1.3).sp
)

val HeadingStyle3 = TextStyle(
    fontFamily = Lora,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = (16 * 1.35).sp
)

val LabelLarge = TextStyle(
    fontFamily = Manrope,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    lineHeight = (14 * 1.4).sp
)

val LabelMedium = TextStyle(
    fontFamily = Manrope,
    fontWeight = FontWeight.SemiBold,
    fontSize = 13.sp,
    lineHeight = (13 * 1.5).sp
)

val LabelSmall = TextStyle(
    fontFamily = Manrope,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = (12 * 1.4).sp
)

val Caption = TextStyle(
    fontFamily = Manrope,
    fontWeight = FontWeight.Bold,
    fontSize = 11.sp,
    lineHeight = (11 * 1.3).sp
)

val Micro = TextStyle(
    fontFamily = Manrope,
    fontWeight = FontWeight.Bold,
    fontSize = 10.sp,
    lineHeight = (10 * 1.3).sp
)
