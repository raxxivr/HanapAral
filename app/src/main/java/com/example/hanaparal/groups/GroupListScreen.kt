package com.example.hanaparal.groups

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GroupListScreen(
    isGroupCreationEnabled: Boolean = true,
    maxMembersPerGroup: Int = 10,
    onCreateGroupClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            if (isGroupCreationEnabled) {
                FloatingActionButton(onClick = onCreateGroupClick) {
                    Icon(Icons.Default.Add, contentDescription = "Create Group")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Study Groups", style = MaterialTheme.typography.headlineMedium)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Max members per group: $maxMembersPerGroup",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (!isGroupCreationEnabled) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Group creation is currently disabled by admin.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "List of groups will appear here...")
            }
        }
    }
}
