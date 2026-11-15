package com.kevin.navigationbackstacklab

import androidx.compose.runtime.Composable

/** iOS has no system Back action: the on-screen Back button is the only way back. */
@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
