package com.example.hanaparal.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.hanaparal.admin.AppConfig
import com.example.hanaparal.auth.BiometricAuthManager
import com.example.hanaparal.viewmodel.RemoteConfigViewModel

private val PrimaryBlue = Color(0xFF1565C0)
private val BackgroundGray = Color(0xFFF8F9FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    remoteConfigViewModel: RemoteConfigViewModel,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val activity = context as? FragmentActivity
    val config by remoteConfigViewModel.config.collectAsState()
    val isAuthenticated by remoteConfigViewModel.isSuperuserAuthenticated.collectAsState()
    
    // Local state for editing before applying
    var editIsGroupCreationEnabled by remember { mutableStateOf(config.isGroupCreationEnabled) }
    var editAnnouncementHeader by remember { mutableStateOf(config.announcementHeader) }
    var editMaxMembers by remember { mutableStateOf(config.maxMembersPerGroup.toString()) }
    var editIsAdminPanelEnabled by remember { mutableStateOf(config.isAdminPanelEnabled) }

    // Sync local state when config changes from Firebase
    LaunchedEffect(config) {
        editIsGroupCreationEnabled = config.isGroupCreationEnabled
        editAnnouncementHeader = config.announcementHeader
        editMaxMembers = config.maxMembersPerGroup.toString()
        editIsAdminPanelEnabled = config.isAdminPanelEnabled
    }

    LaunchedEffect(Unit) {
        if (!isAuthenticated && activity != null) {
            val authManager = BiometricAuthManager(activity)
            if (authManager.isBiometricAvailable()) {
                val success = authManager.authenticateSuperuser()
                remoteConfigViewModel.setSuperuserAuthenticated(success)
                if (!success) {
                    onBackPressed()
                }
            } else {
                remoteConfigViewModel.setSuperuserAuthenticated(true)
            }
        }
    }

    if (!isAuthenticated) {
        Box(modifier = Modifier.fillMaxSize().background(BackgroundGray), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
    } else {
        Scaffold(
            containerColor = BackgroundGray,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("ADMIN PANEL", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
                    actions = {
                        IconButton(onClick = { 
                            remoteConfigViewModel.logoutSuperuser()
                            onBackPressed()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = PrimaryBlue,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    PrimaryBlue,
                                    PrimaryBlue.copy(alpha = 0.7f),
                                    BackgroundGray
                                )
                            )
                        )
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Header space without icon as requested
                }

                Card(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .offset(y = (-20).dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text("Remote Configuration", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = PrimaryBlue)
                        
                        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)

                        // Feature Toggles
                        AdminToggleItem(
                            label = "Enable Group Creation",
                            checked = editIsGroupCreationEnabled,
                            onCheckedChange = { editIsGroupCreationEnabled = it }
                        )

                        AdminToggleItem(
                            label = "Admin Panel Visibility",
                            checked = editIsAdminPanelEnabled,
                            onCheckedChange = { editIsAdminPanelEnabled = it }
                        )

                        OutlinedTextField(
                            value = editMaxMembers,
                            onValueChange = { if (it.all { char -> char.isDigit() }) editMaxMembers = it },
                            label = { Text("Max Members Per Group") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, focusedLabelColor = PrimaryBlue)
                        )

                        OutlinedTextField(
                            value = editAnnouncementHeader,
                            onValueChange = { editAnnouncementHeader = it },
                            label = { Text("Announcement Header") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, focusedLabelColor = PrimaryBlue)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val newConfig = AppConfig(
                                    isGroupCreationEnabled = editIsGroupCreationEnabled,
                                    announcementHeader = editAnnouncementHeader,
                                    maxMembersPerGroup = editMaxMembers.toIntOrNull() ?: 10,
                                    isAdminPanelEnabled = editIsAdminPanelEnabled
                                )
                                remoteConfigViewModel.updateConfig(newConfig)
                                focusManager.clearFocus()
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Apply Changes Locally", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { remoteConfigViewModel.refreshConfig() },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Sync with Firebase Console", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminToggleItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryBlue)
        )
    }
}
