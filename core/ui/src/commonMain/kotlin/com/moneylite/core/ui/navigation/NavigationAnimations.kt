package com.moneylite.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

private const val RootMotionDurationMillis = 280
private const val MainMotionDurationMillis = 220
private const val RootSlideDistanceDivisor = 8
private const val MainSlideDistanceDivisor = 14

private val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

private fun getTabOrder(route: Route): Int? {
    return when (route) {
        is Route.Home -> 0
        is Route.Schedule -> 1
        is Route.Transaction -> 2
        is Route.Profile -> 3
        else -> null
    }
}

fun <T : NavKey> rootForwardTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    val initialRoute = initialState.key as? Route
    val targetRoute = targetState.key as? Route

    when {
        initialRoute is Route.Splash -> {
            // Splash to Onboarding/Login/Main -> Smooth Zoom-Out & Fade
            (fadeIn(animationSpec = tween(400, easing = EmphasizedEasing)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400, easing = EmphasizedEasing))) togetherWith
            (fadeOut(animationSpec = tween(280, easing = EmphasizedEasing)) + scaleOut(targetScale = 1.05f, animationSpec = tween(280, easing = EmphasizedEasing))) using
            SizeTransform(clip = false)
        }
        targetRoute is Route.ForgetPassword -> {
            // Slide left for forgot password
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(320, easing = EmphasizedEasing)
            ) + fadeIn(tween(320, easing = EmphasizedEasing)) togetherWith
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(320, easing = EmphasizedEasing)
            ) + fadeOut(tween(320, easing = EmphasizedEasing)) using SizeTransform(clip = false)
        }
        else -> {
            elegantSlideTransition(
                direction = AnimatedContentTransitionScope.SlideDirection.Left,
                durationMillis = RootMotionDurationMillis,
                slideDistanceDivisor = RootSlideDistanceDivisor
            )
        }
    }
}

fun <T : NavKey> rootBackTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    val initialRoute = initialState.key as? Route

    when {
        initialRoute is Route.ForgetPassword -> {
            // Slide right when going back from forgot password
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(320, easing = EmphasizedEasing)
            ) + fadeIn(tween(320, easing = EmphasizedEasing)) togetherWith
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(320, easing = EmphasizedEasing)
            ) + fadeOut(tween(320, easing = EmphasizedEasing)) using SizeTransform(clip = false)
        }
        else -> {
            elegantSlideTransition(
                direction = AnimatedContentTransitionScope.SlideDirection.Right,
                durationMillis = RootMotionDurationMillis,
                slideDistanceDivisor = RootSlideDistanceDivisor
            )
        }
    }
}

fun <T : NavKey> mainForwardTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    val initialRoute = initialState.key as? Route
    val targetRoute = targetState.key as? Route

    val initialTab = initialRoute?.let { getTabOrder(it) }
    val targetTab = targetRoute?.let { getTabOrder(it) }

    when {
        initialTab != null && targetTab != null -> {
            // Tab-to-Tab transitions: slide left or right depending on target tab index
            val direction = if (targetTab > initialTab) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            tabSlideTransition(direction)
        }
        targetRoute is Route.AddTransaction -> {
            // Add transaction entry: Slide up from bottom (Sheet-style)
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(380, easing = EmphasizedEasing)
            ) + fadeIn(tween(260)) togetherWith
            (fadeOut(animationSpec = tween(260)) + scaleOut(targetScale = 0.96f, animationSpec = tween(260))) using
            SizeTransform(clip = false)
        }
        else -> {
            // Default subpage slide
            elegantSlideTransition(
                direction = AnimatedContentTransitionScope.SlideDirection.Left,
                durationMillis = MainMotionDurationMillis,
                slideDistanceDivisor = MainSlideDistanceDivisor
            )
        }
    }
}

fun <T : NavKey> mainBackTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    val initialRoute = initialState.key as? Route
    val targetRoute = targetState.key as? Route

    val initialTab = initialRoute?.let { getTabOrder(it) }
    val targetTab = targetRoute?.let { getTabOrder(it) }

    when {
        initialTab != null && targetTab != null -> {
            // Tab-to-Tab transitions
            val direction = if (targetTab > initialTab) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            tabSlideTransition(direction)
        }
        initialRoute is Route.AddTransaction -> {
            // Add transaction exit: Slide down to bottom
            (fadeIn(animationSpec = tween(260)) + scaleIn(initialScale = 0.96f, animationSpec = tween(260))) togetherWith
            (slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(340, easing = EmphasizedEasing)
            ) + fadeOut(animationSpec = tween(200))) using SizeTransform(clip = false)
        }
        else -> {
            elegantSlideTransition(
                direction = AnimatedContentTransitionScope.SlideDirection.Right,
                durationMillis = MainMotionDurationMillis,
                slideDistanceDivisor = MainSlideDistanceDivisor
            )
        }
    }
}

private fun <T : NavKey> AnimatedContentTransitionScope<Scene<T>>.tabSlideTransition(
    direction: AnimatedContentTransitionScope.SlideDirection
): ContentTransform {
    val motionSpec = tween<IntOffset>(
        durationMillis = 280,
        easing = EmphasizedEasing
    )
    val fadeSpec = tween<Float>(
        durationMillis = 240,
        easing = EmphasizedEasing
    )

    return (
        slideIntoContainer(
            towards = direction,
            animationSpec = motionSpec,
            initialOffset = { distance -> distance / 12 }
        ) + fadeIn(animationSpec = fadeSpec)
        ) togetherWith (
        slideOutOfContainer(
            towards = direction,
            animationSpec = motionSpec,
            targetOffset = { distance -> distance / 12 }
        ) + fadeOut(animationSpec = fadeSpec)
        ) using SizeTransform(clip = false)
}

private fun <T : NavKey> AnimatedContentTransitionScope<Scene<T>>.elegantSlideTransition(
    direction: AnimatedContentTransitionScope.SlideDirection,
    durationMillis: Int,
    slideDistanceDivisor: Int
): ContentTransform {
    val motionSpec = tween<IntOffset>(
        durationMillis = durationMillis,
        easing = EmphasizedEasing
    )
    val fadeSpec = tween<Float>(
        durationMillis = durationMillis,
        easing = EmphasizedEasing
    )

    return (
        slideIntoContainer(
            towards = direction,
            animationSpec = motionSpec,
            initialOffset = { distance -> distance / slideDistanceDivisor }
        ) + fadeIn(animationSpec = fadeSpec) + scaleIn(
            initialScale = 0.985f,
            animationSpec = fadeSpec
        )
        ) togetherWith (
        slideOutOfContainer(
            towards = direction,
            animationSpec = motionSpec,
            targetOffset = { distance -> distance / (slideDistanceDivisor * 2) }
        ) + fadeOut(animationSpec = fadeSpec) + scaleOut(
            targetScale = 0.995f,
            animationSpec = fadeSpec
        )
        ) using SizeTransform(clip = false)
}
