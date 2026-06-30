package com.moneylite.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Payments
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.moneylite.core.ui.components.AppTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.runtime.remember
import com.moneylite.core.ui.components.RupiahAmountVisualTransformation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneylite.core.ui.theme.BluePrimary
import com.moneylite.core.ui.theme.LocalThemeIsDark
import com.moneylite.core.ui.components.AppPrimaryButton
import com.moneylite.core.ui.components.AppTextButton
import com.moneylite.core.ui.components.WaterDropIndicator
import com.moneylite.core.ui.components.switch_button.SwitchButton
import com.moneylite.core.ui.components.switch_button.SwitchButtonConfig
import com.moneylite.core.ui.components.switch_button.SwitchButtonIcon
import kotlinx.coroutines.launch
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material.icons.Icons
import com.moneylite.core.ui.generated.resources.*

data class OnboardingPage(
    val icon: ImageVector,
    val title: StringResource,
    val description: StringResource
)

sealed interface OnboardingStep {
    data class Info(val page: OnboardingPage) : OnboardingStep
    data object ProfileSetup : OnboardingStep
    data object SalarySetup : OnboardingStep
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onOnboardingComplete: () -> Unit
) {

    val onboardingPages = remember {
        listOf(
            OnboardingPage(
                icon = Icons.Default.SmartToy,
                title = Res.string.welcome_to_app,
                description = Res.string.onboarding_description_1
            ),
            OnboardingPage(
                icon = Icons.Default.CalendarMonth,
                title = Res.string.smart_scheduling,
                description = Res.string.onboarding_description_2
            ),
            OnboardingPage(
                icon = Icons.Default.Notifications,
                title = Res.string.stay_notified,
                description = Res.string.onboarding_description_3
            )
        )
    }

    val steps = remember(onboardingPages) {
        buildList {
            addAll(onboardingPages.map { OnboardingStep.Info(it) })
            add(OnboardingStep.ProfileSetup)
            add(OnboardingStep.SalarySetup)
        }
    }

    val pagerState = rememberPagerState(pageCount = { steps.size })
    val coroutineScope = rememberCoroutineScope()
    val viewModel = koinViewModel<OnboardingViewModel>()
    
    val state by viewModel.state.collectAsStateWithLifecycle()
    val themeState = LocalThemeIsDark.current

    LaunchedEffect(state.isDarkMode) {
        state.isDarkMode?.let {
            themeState.value = it
        }
    }
    
    LaunchedEffect(state.isOnboardingCompleted) {
        if (state.isOnboardingCompleted) {
            onOnboardingComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    AnimatedVisibility(
                        visible = steps[pagerState.currentPage] is OnboardingStep.Info,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        AppTextButton(
                            text = stringResource(Res.string.skip),
                            onClick = {
                                viewModel.onEvent(OnboardingEvent.CompleteOnboarding)
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 16.dp)) {
                        val isDark = state.isDarkMode ?: themeState.value
                        SwitchButton(
                            isSelected = isDark,
                            onStateChange = { viewModel.onEvent(OnboardingEvent.ToggleDarkMode(it)) },
                            icon = {
                                SwitchButtonIcon(
                                    isSelected = isDark,
                                    selectedIcon = Icons.Filled.WbSunny,
                                    iconColor = BluePrimary,
                                    unSelectedIcon = Icons.Filled.DarkMode,
                                    contentDescription = if (isDark) stringResource(Res.string.switch_to_light_mode) else stringResource(
                                        Res.string.switch_to_dark_mode
                                    )
                                )
                            },
                            switchButtonConfig = SwitchButtonConfig(
                                selectedBackgroundColor = MaterialTheme.colorScheme.primary,
//                                innerBoxColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val gradientColors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            MaterialTheme.colorScheme.background
        )
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .background(Brush.verticalGradient(gradientColors))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pager content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    when (val step = steps[page]) {
                        is OnboardingStep.Info -> OnboardingPageContent(page = step.page, pageOffset = pageOffset)
                        OnboardingStep.ProfileSetup -> ProfileSetupScreenContent(state = state, onEvent = viewModel::onEvent, pageOffset = pageOffset)
                        OnboardingStep.SalarySetup -> SalarySetupScreenContent(state = state, onEvent = viewModel::onEvent, pageOffset = pageOffset)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Page indicator dots with animation
                WaterDropIndicator(
                    pagerState = pagerState,
                    modifier = Modifier.padding(16.dp),
                    activeColor = MaterialTheme.colorScheme.primary,
                    inactiveColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Navigation buttons
                val isLastPage = pagerState.currentPage == steps.lastIndex
                val currentStep = steps[pagerState.currentPage]

                when (currentStep) {
                    is OnboardingStep.SalarySetup -> {
                        AppPrimaryButton(
                            text = stringResource(Res.string.get_started),
                            onClick = {
                                viewModel.onEvent(OnboardingEvent.SaveProfileAndComplete)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is OnboardingStep.ProfileSetup -> {
                        AppPrimaryButton(
                            text = stringResource(Res.string.next),
                            onClick = {
                                if (state.isProfileValid) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                } else {
                                    viewModel.onEvent(OnboardingEvent.NameChanged(state.name))
                                    viewModel.onEvent(OnboardingEvent.JobChanged(state.job))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        AppPrimaryButton(
                            text = stringResource(Res.string.next),
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageOffset: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yOffset"
    )
    val floatRotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Page offset transitions
    val absOffset = pageOffset.absoluteValue
    val scale = (1f - absOffset * 0.2f).coerceIn(0.8f, 1f)
    val alpha = (1f - absOffset).coerceIn(0f, 1f)
    val slideX = pageOffset * 200f // Slide horizontal direction opposite to swipe

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.alpha = alpha
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated layered glow card
        Box(
            modifier = Modifier
                .size(220.dp)
                .graphicsLayer {
                    this.scaleX = scale
                    this.scaleY = scale
                    this.translationY = floatOffset
                    this.rotationZ = floatRotation
                },
            contentAlignment = Alignment.Center
        ) {
            // Background Glow
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    )
            )

            // Main card with gradient background
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            )
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(page.title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .graphicsLayer {
                    translationX = slideX * 1.2f
                }
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(page.description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .graphicsLayer {
                    translationX = slideX * 1.5f
                }
                .padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun ProfileSetupScreenContent(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
    pageOffset: Float
) {
    val focusManager = LocalFocusManager.current
    val absOffset = pageOffset.absoluteValue
    val scale = (1f - absOffset * 0.15f).coerceIn(0.85f, 1f)
    val alpha = (1f - absOffset).coerceIn(0f, 1f)
    val slideX = pageOffset * 150f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            }
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        )
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.onboarding_profile_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                translationX = slideX * 1.2f
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.onboarding_profile_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                translationX = slideX * 1.4f
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = slideX * 1.6f
                }
        ) {
            AppTextField(
                value = state.name,
                onValueChange = { onEvent(OnboardingEvent.NameChanged(it)) },
                label = stringResource(Res.string.name),
                placeholder = "e.g., John Doe",
                isError = state.nameError,
                errorMessage = if (state.nameError) stringResource(Res.string.name_required) else null,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = state.job,
                onValueChange = { onEvent(OnboardingEvent.JobChanged(it)) },
                label = stringResource(Res.string.job),
                placeholder = "e.g., Software Engineer",
                isError = state.jobError,
                errorMessage = if (state.jobError) stringResource(Res.string.job_required) else null,
                imeAction = ImeAction.Done,
                onImeAction = { focusManager.clearFocus() }
            )
        }
    }
}

@Composable
private fun SalarySetupScreenContent(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
    pageOffset: Float
) {
    val absOffset = pageOffset.absoluteValue
    val scale = (1f - absOffset * 0.15f).coerceIn(0.85f, 1f)
    val alpha = (1f - absOffset).coerceIn(0f, 1f)
    val slideX = pageOffset * 150f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            }
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        )
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Payments,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.onboarding_salary_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                translationX = slideX * 1.2f
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.onboarding_salary_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                translationX = slideX * 1.4f
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = slideX * 1.6f
                }
        ) {
            AppTextField(
                value = state.salary,
                onValueChange = { input -> 
                    if (input.all { it.isDigit() }) {
                        onEvent(OnboardingEvent.SalaryChanged(input))
                    }
                },
                label = stringResource(Res.string.salary_label),
                placeholder = "e.g., 10000000",
                isError = state.salaryError,
                errorMessage = if (state.salaryError) stringResource(Res.string.salary_required) else null,
                keyboardType = KeyboardType.Number,
                visualTransformation = RupiahAmountVisualTransformation,
                imeAction = ImeAction.Done,
                onImeAction = { onEvent(OnboardingEvent.SaveProfileAndComplete) }
            )
        }
    }
}
