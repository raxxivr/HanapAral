package com.example.hanaparal.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.hanaparal.admin.BiometricAuthManager
import com.example.hanaparal.viewmodel.RemoteConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    remoteConfigViewModel: RemoteConfigViewModel,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val config by remoteConfigViewModel.config.collectAsState()
    val isAuthenticated by remoteConfigViewModel.isSuperuserAuthenticated.collectAsState()
    val showAuthDialog by remoteConfigViewModel.showAuthDialog.collectAsState()
    
    val scope = rememberCoroutineScope()

    // Trigger biometric authentication on entry
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
                // For demo purposes, if biometrics are not available, we'll allow access 
                // but in a real app you'd handle this more strictly.
                remoteConfigViewModel.setSuperuserAuthenticated(true)
            }
        }
    }

    if (!isAuthenticated) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Panel") },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            remoteConfigViewModel.logoutSuperuser()
                            onBackPressed()
                        }) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Firebase Remote Config",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Current live values from Firebase Console",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                AdminConfigCard(
                    title = "Group Settings",
                    items = listOf(
                        "Creation Enabled" to config.isGroupCreationEnabled.toString(),
                        "Max Members" to config.maxMembersPerGroup.toString()
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AdminConfigCard(
                    title = "UI Settings",
                    items = listOf(
                        "Announcement" to config.announcementHeader,
                        "Admin Panel Enabled" to config.isAdminPanelEnabled.toString()
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Feature Toggles (Read-only)",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ToggleItem("Enable Group Creation", config.isGroupCreationEnabled)
                ToggleItem("Show Announcement Header", config.announcementHeader.isNotEmpty())
                ToggleItem("Admin Panel Accessible", config.isAdminPanelEnabled)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { remoteConfigViewModel.refreshConfig() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Force Fetch & Activate")
                }
            }
        }
    }
}

@Composable
fun AdminConfigCard(title: String, items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                    Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ToggleItem(label: String, checked: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Switch(checked = checked, onCheckedChange = null, enabled = false)
    }
}
