package com.kevin.navigationbackstacklab

import androidx.compose.runtime.Composable

/**
 * Routes the platform Back action (Android back button / gesture) to [onBack] while [enabled].
 * When disabled, Back falls through to the system (closing the app at the root).
 */
@Composable
expect fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit)
