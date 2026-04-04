package com.example.hanaparal.groups

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hanaparal.models.StudyGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun GroupListScreen(onCreateGroupClick: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val currentUserId = auth.currentUser?.uid

    var groups by remember { mutableStateOf<List<StudyGroup>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedGroupForDetail by remember { mutableStateOf<StudyGroup?>(null) }

    val tabs = listOf("Available Groups", "My Groups")

    LaunchedEffect(Unit) {
        // Using snapshot listener for real-time updates when joining
        db.collection("groups")
            .addSnapshotListener { result, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (result != null) {
                    groups = result.toObjects(StudyGroup::class.java)
                }
                isLoading = false
            }
    }

    val filteredGroups = remember(groups, selectedTabIndex, currentUserId) {
        if (selectedTabIndex == 0) {
            // Available: Not a member
            groups.filter { !it.members.contains(currentUserId) }
        } else {
            // My Groups: Is a member
            groups.filter { it.members.contains(currentUserId) }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateGroupClick) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredGroups) { group ->
                        GroupItem(
                            group = group,
                            currentUserId = currentUserId,
                            onCardClick = { selectedGroupForDetail = group },
                            onJoinClick = {
                                if (currentUserId != null) {
                                    db.collection("groups").document(group.id)
                                        .update("members", FieldValue.arrayUnion(currentUserId))
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Joined ${group.name}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        )
                    }
                    if (filteredGroups.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (selectedTabIndex == 0) "No available groups" else "You haven't joined any groups yet",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    selectedGroupForDetail?.let { group ->
        AlertDialog(
            onDismissRequest = { selectedGroupForDetail = null },
            title = { Text(text = group.name, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "Subject: ${group.subject}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Description:", fontWeight = FontWeight.Bold)
                    Text(text = group.description)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Members: ${group.members.size}", fontWeight = FontWeight.Bold)
                    group.members.forEach { memberId ->
                        Text(text = "• $memberId", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedGroupForDetail = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun GroupItem(
    group: StudyGroup,
    currentUserId: String?,
    onCardClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    val isMember = currentUserId != null && group.members.contains(currentUserId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = group.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "Subject: ${group.subject}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Members: ${group.members.size}")
                if (!isMember) {
                    Button(onClick = onJoinClick) {
                        Text("Join")
                    }
                } else {
                    AssistChip(
                        onClick = { onCardClick() },
                        label = { Text("Member") }
                    )
                }
            }
        }
    }
}
