package com.example.hanaparal.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    isAdminPanelEnabled: Boolean = false,
    onAdminClick: () -> Unit,
    onNavigateToGroups: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Profile Screen", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onNavigateToGroups) {
                Text(text = "Browse Groups")
            }

            if (isAdminPanelEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAdminClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(text = "Admin Panel")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Member 2: Add Profile Details Here", style = MaterialTheme.typography.bodySmall)
        }
    }
}
