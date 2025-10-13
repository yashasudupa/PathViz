package com.urbansetu.app.home

import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.urbansetu.app.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    // Trigger fade-in
    LaunchedEffect(Unit) {
        visible = true
        delay(2500) // splash duration ~2.5s
        visible = false
        delay(400) // allow fade-out
        onFinish()
    }

    // Fade-in alpha animation
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(1000) // 1s fade
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // optional background
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.urbansetu_logo),
            contentDescription = "UrbanSetu Logo",
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .alpha(alpha),
            contentScale = ContentScale.Fit
        )
    }
}
