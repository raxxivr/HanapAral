package com.example.hanaparal.groups

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hanaparal.models.StudyGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun GroupListScreen(
    isGroupCreationEnabled: Boolean = true,
    maxMembersPerGroup: Int = 10,
    onCreateGroupClick: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val currentUserId = auth.currentUser?.uid

    var groups by remember { mutableStateOf<List<StudyGroup>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("groups")
            .get()
            .addOnSuccessListener { result ->
                groups = result.toObjects(StudyGroup::class.java)
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        floatingActionButton = {
            if (isGroupCreationEnabled) {
                FloatingActionButton(onClick = onCreateGroupClick) {
                    Icon(Icons.Default.Add, contentDescription = "Create Group")
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(text = "Available Study Groups", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(groups) { group ->
                    GroupItem(
                        group = group,
                        currentUserId = currentUserId,
                        maxMembers = maxMembersPerGroup,
                        onJoinClick = {
                            if (currentUserId != null) {
                                if (group.members.size >= maxMembersPerGroup) {
                                    Toast.makeText(context, "Group is full!", Toast.LENGTH_SHORT).show()
                                    return@GroupItem
                                }
                                
                                db.collection("groups").document(group.id)
                                    .update("members", FieldValue.arrayUnion(currentUserId))
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Joined ${group.name}", Toast.LENGTH_SHORT).show()
                                        // Refresh groups or update local state
                                        groups = groups.map {
                                            if (it.id == group.id) it.copy(members = it.members + currentUserId) else it
                                        }
                                    }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GroupItem(
    group: StudyGroup, 
    currentUserId: String?, 
    maxMembers: Int,
    onJoinClick: () -> Unit
) {
    val isMember = currentUserId != null && group.members.contains(currentUserId)
    val isFull = group.members.size >= maxMembers

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = group.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "Subject: ${group.subject}", style = MaterialTheme.typography.bodyMedium)
            Text(text = group.description, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Members: ${group.members.size} / $maxMembers",
                    color = if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                if (!isMember) {
                    Button(
                        onClick = onJoinClick,
                        enabled = !isFull
                    ) {
                        Text(if (isFull) "Full" else "Join")
                    }
                } else {
                    Text(text = "Joined", color = MaterialTheme.colorScheme.primary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        }
    }
}
