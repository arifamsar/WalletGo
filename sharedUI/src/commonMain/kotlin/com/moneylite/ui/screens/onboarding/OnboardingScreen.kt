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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneylite.core.ui.components.AppPrimaryButton
import com.moneylite.core.ui.components.AppTextField
import com.moneylite.core.ui.components.RupiahAmountVisualTransformation
import com.moneylite.core.ui.components.WaterDropIndicator
import com.moneylite.core.ui.theme.LocalThemeIsDark
import com.moneylite.core.ui.components.AppTextButton
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
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

@OptIn(ExperimentalFoundationApi::class)
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

    // Page offset transitions
    val absOffset = pageOffset.absoluteValue
    val scale = (1f - absOffset * 0.15f).coerceIn(0.85f, 1f)
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
                .graphicsLayer {
                    this.scaleX = scale
                    this.scaleY = scale
                    this.translationY = floatOffset
                    this.rotationZ = floatRotation
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
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
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
                          tint = MaterialTheme.colorScheme.primary
                      )
                  }
              }

              // Decorative floating badge 2 (in front)
              Card(
                  modifier = Modifier
                      .size(54.dp)
                      .align(Alignment.BottomEnd)
                      .graphicsLayer {
                          translationY = -floatOffset * 0.6f
                          translationX = -floatOffset * 0.4f
                      },
                  shape = CircleShape,
                  colors = CardDefaults.cardColors(
                      containerColor = MaterialTheme.colorScheme.secondaryContainer
                  ),
                  elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
              ) {
                  Box(
                      modifier = Modifier.fillMaxSize(),
                      contentAlignment = Alignment.Center
                  ) {
                      Icon(
                          imageVector = when (page.icon) {
                              Icons.Default.SmartToy -> Icons.Default.Payments
                              Icons.Default.CalendarMonth -> Icons.Default.CalendarMonth
                              else -> Icons.Default.Notifications
                          },
                          contentDescription = null,
                          modifier = Modifier.size(24.dp),
                          tint = MaterialTheme.colorScheme.onSecondaryContainer
                      )
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
        // Glowing Icon Badge
        Card(
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

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
        // Glowing Icon Badge
        Card(
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

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
