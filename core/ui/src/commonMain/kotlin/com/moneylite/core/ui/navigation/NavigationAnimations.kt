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

fun <T : NavKey> rootForwardTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    elegantSlideTransition(
        direction = AnimatedContentTransitionScope.SlideDirection.Left,
        durationMillis = RootMotionDurationMillis,
        slideDistanceDivisor = RootSlideDistanceDivisor
    )
}

fun <T : NavKey> rootBackTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    elegantSlideTransition(
        direction = AnimatedContentTransitionScope.SlideDirection.Right,
        durationMillis = RootMotionDurationMillis,
        slideDistanceDivisor = RootSlideDistanceDivisor
    )
}

fun <T : NavKey> mainForwardTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    elegantSlideTransition(
        direction = AnimatedContentTransitionScope.SlideDirection.Left,
        durationMillis = MainMotionDurationMillis,
        slideDistanceDivisor = MainSlideDistanceDivisor
    )
}

fun <T : NavKey> mainBackTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    elegantSlideTransition(
        direction = AnimatedContentTransitionScope.SlideDirection.Right,
        durationMillis = MainMotionDurationMillis,
        slideDistanceDivisor = MainSlideDistanceDivisor
    )
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
