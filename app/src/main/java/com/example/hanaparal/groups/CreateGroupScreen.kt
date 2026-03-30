package com.example.hanaparal.groups

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    maxMembers: Int = 10,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Group") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Group Member Limit: ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$maxMembers members",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Member 3: Implement the rest of the form fields here
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { /* TODO: Save to Firestore */ },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        val userId = auth.currentUser?.uid
                        if (userId != null && name.isNotBlank() && subject.isNotBlank()) {
                            isLoading = true
                            val newGroupRef = db.collection("groups").document()
                            val newGroup = StudyGroup(
                                id = newGroupRef.id,
                                name = name,
                                description = description,
                                subject = subject,
                                creatorId = userId,
                                members = listOf(userId)
                            )
                            newGroupRef.set(newGroup)
                                .addOnSuccessListener {
                                    isLoading = false
                                    Toast.makeText(context, "Group Created!", Toast.LENGTH_SHORT).show()
                                    onBack() // Go back after success
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Group")
                }
            }
        }
    }
}
