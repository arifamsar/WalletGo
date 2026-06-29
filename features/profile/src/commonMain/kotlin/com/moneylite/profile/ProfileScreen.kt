@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class
)
package com.moneylite.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneylite.core.ui.adaptive.AdaptiveWindowBox
import com.moneylite.core.ui.adaptive.AdaptiveWindowClass
import com.moneylite.core.ui.adaptive.isExpanded
import com.moneylite.core.ui.navigation.Route
import com.moneylite.core.ui.components.AppDialog
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import com.moneylite.core.ui.generated.resources.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.moneylite.core.domain.model.formatToRupiah


data class ProfileMenuItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val hasSwitch: Boolean = false,
    val isDestructive: Boolean = false,
    val route: Route? = null
)

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigate: (Route) -> Unit = {}
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val darkModeEnabled by viewModel.darkModeEnabled.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userJob by viewModel.userJob.collectAsStateWithLifecycle()
    val userSalary by viewModel.userSalary.collectAsStateWithLifecycle()
    var showEditProfileDialog by remember { mutableStateOf(false) }

    AdaptiveWindowBox(modifier = modifier) { windowClass ->
        ProfileScreenContent(
            userName = userName,
            userJob = userJob,
            userSalary = userSalary,
            darkModeEnabled = darkModeEnabled,
            showEditProfileDialog = showEditProfileDialog,
            windowClass = windowClass,
            onDarkModeChange = viewModel::toggleDarkMode,
            onShowEditProfileDialogChange = { showEditProfileDialog = it },
            onUpdateProfile = viewModel::updateProfile,
            onNavigate = onNavigate
        )
    }
}

@Composable
fun ProfileScreenContent(
    userName: String,
    userJob: String,
    userSalary: Long,
    darkModeEnabled: Boolean,
    showEditProfileDialog: Boolean,
    windowClass: AdaptiveWindowClass,
    onDarkModeChange: (Boolean) -> Unit,
    onShowEditProfileDialogChange: (Boolean) -> Unit,
    onUpdateProfile: (String, String, Long) -> Unit,
    modifier: Modifier = Modifier,
    onNavigate: (Route) -> Unit = {}
) {
    val editProfileTitle = stringResource(Res.string.edit_profile)
    val editProfileSubtitle = stringResource(Res.string.edit_profile_subtitle)
    val darkModeTitle = stringResource(Res.string.dark_mode)
    val notificationSettingsTitle = stringResource(Res.string.notification_settings)
    val notificationSettingsSubtitle = stringResource(Res.string.notification_settings_subtitle)
    val appSettingsTitle = stringResource(Res.string.app_settings)
    val appSettingsSubtitle = stringResource(Res.string.app_settings_subtitle)

    val generalSettings = remember(editProfileTitle, editProfileSubtitle) {
        listOf(
            ProfileMenuItem(
                title = editProfileTitle,
                subtitle = editProfileSubtitle,
                icon = Icons.Default.Person
            )
        )
    }

    val preferences = remember(darkModeTitle, notificationSettingsTitle, notificationSettingsSubtitle, appSettingsTitle, appSettingsSubtitle) {
        listOf(
            ProfileMenuItem(
                title = darkModeTitle,
                icon = Icons.Default.DarkMode,
                hasSwitch = true
            ),
            ProfileMenuItem(
                title = notificationSettingsTitle,
                subtitle = notificationSettingsSubtitle,
                icon = Icons.Default.Notifications,
                route = Route.NotificationSettings
            ),
            ProfileMenuItem(
                title = appSettingsTitle,
                subtitle = appSettingsSubtitle,
                icon = Icons.Default.Settings,
                route = Route.Settings
            )
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.profile),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = if (windowClass.isExpanded) 760.dp else Dp.Unspecified),
                contentPadding = PaddingValues(
                    start = if (windowClass.isExpanded) 24.dp else 16.dp,
                    end = if (windowClass.isExpanded) 24.dp else 16.dp,
                    top = 8.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    ProfileCard(
                        userName = userName,
                        userJob = userJob,
                        userSalary = userSalary,
                        onEditClick = { onShowEditProfileDialogChange(true) }
                    )
                }

                item {
                    SettingsSection(
                        title = stringResource(Res.string.general),
                        items = generalSettings,
                        onClick = { item ->
                            if (item.title == editProfileTitle) {
                                onShowEditProfileDialogChange(true)
                            }
                        }
                    )
                }

                item {
                    SettingsSection(
                        title = stringResource(Res.string.preferences),
                        items = preferences,
                        switchStates = mapOf(darkModeTitle to darkModeEnabled),
                        onSwitchChange = { title, value ->
                            if (title == darkModeTitle) onDarkModeChange(value)
                        },
                        onClick = { item ->
                            item.route?.let { onNavigate(it) }
                        }
                    )
                }

                item {
                    Text(
                        text = stringResource(Res.string.app_version_format, "1.0.0"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userName) }
        var tempJob by remember { mutableStateOf(userJob) }
        var tempSalary by remember { mutableStateOf(userSalary.toString()) }

        AppDialog(
            title = stringResource(Res.string.edit_profile),
            confirmText = stringResource(Res.string.save),
            onConfirm = {
                if (tempName.isNotBlank() && tempJob.isNotBlank() && tempSalary.isNotBlank()) {
                    val salaryLong = tempSalary.toLongOrNull() ?: 0L
                    onUpdateProfile(tempName, tempJob, salaryLong)
                    onShowEditProfileDialogChange(false)
                }
            },
            dismissText = stringResource(Res.string.cancel),
            onDismiss = { onShowEditProfileDialogChange(false) },
            content = {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text(stringResource(Res.string.name)) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempJob,
                        onValueChange = { tempJob = it },
                        label = { Text(stringResource(Res.string.job)) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempSalary,
                        onValueChange = { input -> 
                            if (input.all { it.isDigit() }) {
                                tempSalary = input
                            }
                        },
                        label = { Text(stringResource(Res.string.monthly_salary)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    items: List<ProfileMenuItem>,
    modifier: Modifier = Modifier,
    switchStates: Map<String, Boolean> = emptyMap(),
    onSwitchChange: (String, Boolean) -> Unit = { _, _ -> },
    onClick: (ProfileMenuItem) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        SettingsGroup(
            items = items,
            switchStates = switchStates,
            onSwitchChange = onSwitchChange,
            onClick = onClick
        )
    }
}

@Composable
private fun ProfileCard(
    userName: String,
    userJob: String,
    userSalary: Long,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val initials = userName.split(" ")
                .mapNotNull { it.firstOrNull() }
                .take(2)
                .joinToString("")
                .uppercase()

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials.ifEmpty { "ML" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$userJob • ${userSalary.formatToRupiah()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                )
            }
            
            IconButton(
                shapes = IconButtonDefaults.shapes(),
                onClick = onEditClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.42f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(Res.string.edit_profile)
                )
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    items: List<ProfileMenuItem>,
    modifier: Modifier = Modifier,
    switchStates: Map<String, Boolean> = emptyMap(),
    onSwitchChange: (String, Boolean) -> Unit = { _, _ -> },
    onClick: (ProfileMenuItem) -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column {
            items.forEachIndexed { index, item ->
                SettingsItem(
                    item = item,
                    switchState = switchStates[item.title],
                    onSwitchChange = { onSwitchChange(item.title, it) },
                    onClick = { onClick(item) }
                )
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    item: ProfileMenuItem,
    switchState: Boolean?,
    onSwitchChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val contentColor = if (item.isDestructive)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.onSurface

    val rowModifier = if (item.hasSwitch && switchState != null) {
        Modifier
            .toggleable(
                value = switchState,
                role = Role.Switch,
                onValueChange = onSwitchChange
            )
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp)
            .padding(16.dp)
    } else {
        Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp)
            .padding(16.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(
                    if (item.isDestructive) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.44f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (item.isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            if (item.subtitle != null) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (item.hasSwitch && switchState != null) {
            Switch(
                checked = switchState,
                onCheckedChange = null
            )
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
