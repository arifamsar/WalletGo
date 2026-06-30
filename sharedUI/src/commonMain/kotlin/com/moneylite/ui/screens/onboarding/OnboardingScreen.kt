package com.moneylite.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneylite.core.ui.components.AppPrimaryButton
import com.moneylite.core.ui.components.AppTextButton
import com.moneylite.core.ui.components.AppTextField
import com.moneylite.core.ui.components.RupiahAmountVisualTransformation
import com.moneylite.core.ui.components.WaterDropIndicator
import com.moneylite.core.ui.generated.resources.Res
import com.moneylite.core.ui.generated.resources.get_started
import com.moneylite.core.ui.generated.resources.job
import com.moneylite.core.ui.generated.resources.job_required
import com.moneylite.core.ui.generated.resources.name
import com.moneylite.core.ui.generated.resources.name_required
import com.moneylite.core.ui.generated.resources.next
import com.moneylite.core.ui.generated.resources.onboarding_description_1
import com.moneylite.core.ui.generated.resources.onboarding_description_2
import com.moneylite.core.ui.generated.resources.onboarding_description_3
import com.moneylite.core.ui.generated.resources.onboarding_profile_description
import com.moneylite.core.ui.generated.resources.onboarding_profile_title
import com.moneylite.core.ui.generated.resources.onboarding_salary_description
import com.moneylite.core.ui.generated.resources.onboarding_salary_title
import com.moneylite.core.ui.generated.resources.salary_label
import com.moneylite.core.ui.generated.resources.salary_required
import com.moneylite.core.ui.generated.resources.skip
import com.moneylite.core.ui.generated.resources.smart_scheduling
import com.moneylite.core.ui.generated.resources.stay_notified
import com.moneylite.core.ui.generated.resources.welcome_to_app
import com.moneylite.core.ui.theme.LocalThemeIsDark
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.absoluteValue

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onOnboardingComplete: () -> Unit
) {

    val onboardingPages = remember {
        listOf(
            OnboardingPage(
                icon = Icons.Default.TrendingUp,
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
    ) { innerPadding ->
        val targetStartColor = when (pagerState.currentPage) {
            0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            1 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)
            2 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
            3 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
        }
        val startColor by animateColorAsState(
            targetValue = targetStartColor,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            label = "startColorTransition"
        )
        val gradientColors = listOf(startColor, MaterialTheme.colorScheme.background)

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Step Progress Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    steps.forEachIndexed { index, _ ->
                        val isCurrent = index == pagerState.currentPage
                        val isVisited = index < pagerState.currentPage
                        val progressColor = if (isCurrent || isVisited) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        }
                        val widthWeight by animateFloatAsState(
                            targetValue = if (isCurrent) 2.5f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "progressWeight"
                        )
                        Box(
                            modifier = Modifier
                                .weight(widthWeight)
                                .height(4.dp)
                                .background(
                                    color = progressColor,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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

                val currentStep = steps[pagerState.currentPage]

                if (currentStep is OnboardingStep.Info) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppTextButton(
                            text = stringResource(Res.string.skip),
                            onClick = {
                                viewModel.onEvent(OnboardingEvent.CompleteOnboarding)
                            },
                            modifier = Modifier.width(90.dp)
                        )

                        WaterDropIndicator(
                            pagerState = pagerState,
                            activeColor = MaterialTheme.colorScheme.primary,
                            inactiveColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        AppPrimaryButton(
                            text = stringResource(Res.string.next),
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier.width(110.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WaterDropIndicator(
                            pagerState = pagerState,
                            modifier = Modifier.padding(bottom = 24.dp),
                            activeColor = MaterialTheme.colorScheme.primary,
                            inactiveColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

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
                            else -> {}
                        }
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
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yOffset"
    )
    val floatRotation by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Tap Interaction State for playful micro-interactions
    var tapCount by remember { mutableStateOf(0) }
    val interactiveScale by animateFloatAsState(
        targetValue = if (tapCount % 2 != 0) 1.12f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tapScale"
    )

    val bellRotation by animateFloatAsState(
        targetValue = if (tapCount > 0 && page.icon == Icons.Default.Notifications) {
            if (tapCount % 2 == 0) -15f else 15f
        } else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bellRotation"
    )

    // Page offset transitions
    val absOffset = pageOffset.absoluteValue
    val scale = (1f - absOffset * 0.15f).coerceIn(0.85f, 1f) * interactiveScale
    val alpha = (1f - absOffset).coerceIn(0f, 1f)
    val slideX = pageOffset * 220f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.alpha = alpha
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Premium Layered Composition
        Box(
            modifier = Modifier
                .size(240.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    tapCount++
                }
                .graphicsLayer {
                    this.scaleX = scale
                    this.scaleY = scale
                    this.translationY = floatOffset
                    this.rotationZ = floatRotation + bellRotation
                },
            contentAlignment = Alignment.Center
        ) {
            // Glowing background aura
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (tapCount % 2 != 0) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Decorative floating badge 1 (behind)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.TopStart)
                    .graphicsLayer {
                        translationY = floatOffset * 0.5f
                        translationX = floatOffset * 0.3f
                        rotationZ = if (tapCount % 2 != 0) 45f else 0f
                    }
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            // Main glassmorphic container
            Card(
                modifier = Modifier.size(170.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            Color.Transparent,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        )
                    )
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        modifier = Modifier.size(76.dp),
                        tint = if (tapCount % 2 != 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Decorative floating badge 2 (in front)
            Card(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.BottomEnd)
                    .graphicsLayer {
                        translationY = -floatOffset * 0.6f
                        translationX = -floatOffset * 0.4f
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (page.icon == Icons.Default.TrendingUp) {
                        Text(
                            text = if (tapCount % 2 != 0) "+15% 📈" else "Growth",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (page.icon == Icons.Default.CalendarMonth) {
                        Text(
                            text = if (tapCount % 2 != 0) "Paid! ✓" else "Schedule",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = if (tapCount % 2 != 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(44.dp))

        Text(
            text = stringResource(page.title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .graphicsLayer {
                    translationX = slideX * 1.1f
                }
                .padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(page.description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .graphicsLayer {
                    translationX = slideX * 1.3f
                }
                .padding(horizontal = 40.dp)
        )
    }
}

private fun formatRupiah(amountStr: String): String {
    val amount = amountStr.toLongOrNull() ?: return "Rp 0"
    val formatter = amount.toString().reversed().chunked(3).joinToString(".").reversed()
    return "Rp $formatter"
}

@Composable
private fun LiveMemberCard(
    name: String,
    job: String,
    salary: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    Color.Transparent
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
        ) {
            // Chip and Brand Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold Chip lookalike
                Box(
                    modifier = Modifier
                        .size(36.dp, 26.dp)
                        .background(
                            color = Color(0xFFE5A93B),
                            shape = RoundedCornerShape(6.dp)
                        )
                )
                // App Logo Title
                Text(
                    text = "MONEYLITE",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
            }

            // Card Holder Info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = if (name.isBlank()) "YOUR NAME" else name.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (job.isBlank()) "OCCUPATION" else job.uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // Salary Info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "EST. INCOME",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatRupiah(salary),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
    val scale = (1f - absOffset * 0.12f).coerceIn(0.88f, 1f)
    val alpha = (1f - absOffset).coerceIn(0f, 1f)
    val slideX = pageOffset * 180f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            }
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Dynamic Live Member Card
        LiveMemberCard(
            name = state.name,
            job = state.job,
            salary = state.salary
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(Res.string.onboarding_profile_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                translationX = slideX * 1.1f
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(Res.string.onboarding_profile_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                translationX = slideX * 1.3f
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Premium Glassmorphic Card Container for Inputs
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = slideX * 1.5f
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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

                Spacer(modifier = Modifier.height(18.dp))

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
}

@Composable
private fun SalarySetupScreenContent(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
    pageOffset: Float
) {
    val absOffset = pageOffset.absoluteValue
    val scale = (1f - absOffset * 0.12f).coerceIn(0.88f, 1f)
    val alpha = (1f - absOffset).coerceIn(0f, 1f)
    val slideX = pageOffset * 180f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            }
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Dynamic Live Member Card
        LiveMemberCard(
            name = state.name,
            job = state.job,
            salary = state.salary
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(Res.string.onboarding_salary_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                translationX = slideX * 1.1f
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(Res.string.onboarding_salary_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                translationX = slideX * 1.3f
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Premium Glassmorphic Card Container for Inputs
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = slideX * 1.5f
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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
}
