package com.example.hanaparal.groups

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.models.StudyGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

private val PrimaryBlue = Color(0xFF1565C0)
private val BackgroundGray = Color(0xFFF8F9FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(
    isGroupCreationEnabled: Boolean = true,
    maxMembersPerGroup: Int = 10,
    onCreateGroupClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val currentUserId = auth.currentUser?.uid

    var groups by remember { mutableStateOf<List<StudyGroup>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedGroupForDetail by remember { mutableStateOf<StudyGroup?>(null) }

    LaunchedEffect(Unit) {
        db.collection("groups")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    Toast.makeText(context, "Failed to load groups", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    groups = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(StudyGroup::class.java)?.copy(id = doc.id)
                    }
                    isLoading = false

                    groups.forEach { group ->
                        if (group.members.contains(currentUserId)) {
                            FirebaseMessaging.getInstance().subscribeToTopic("group_${group.id}")
                        }
                    }
                }
            }
    }

    val filteredGroups = remember(groups, selectedTabIndex, currentUserId) {
        if (selectedTabIndex == 0) {
            // My Groups: Is a member
            groups.filter { it.members.contains(currentUserId) }
        } else {
            // Available: Not a member
            groups.filter { !it.members.contains(currentUserId) }
        }
    }

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("STUDY GROUPS", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryBlue,
                        titleContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                        }
                    },
                    navigationIcon = {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = PrimaryBlue,
                    divider = {
                        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                    },
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = PrimaryBlue
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { 
                            Text(
                                "My Groups", 
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTabIndex == 0) PrimaryBlue else Color.Gray
                            ) 
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { 
                            Text(
                                "Available", 
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTabIndex == 1) PrimaryBlue else Color.Gray
                            ) 
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (isGroupCreationEnabled) {
                FloatingActionButton(
                    onClick = onCreateGroupClick,
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Group")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (filteredGroups.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (selectedTabIndex == 0) "You haven't joined any groups yet." else "No study groups available.",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredGroups) { group ->
                        GroupItem(
                            group = group,
                            currentUserId = currentUserId,
                            maxMembers = maxMembersPerGroup,
                            onCardClick = { selectedGroupForDetail = group },
                            onJoinClick = {
                                if (currentUserId != null) {
                                    if (group.members.size >= maxMembersPerGroup) {
                                        Toast.makeText(context, "Group is full!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        db.collection("groups").document(group.id)
                                            .update("members", FieldValue.arrayUnion(currentUserId))
                                            .addOnSuccessListener {
                                                FirebaseMessaging.getInstance().subscribeToTopic("group_${group.id}")
                                                Toast.makeText(context, "Joined ${group.name}", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "Failed to join group", Toast.LENGTH_SHORT).show()
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
                    Text(text = "Members: ${group.members.size} / $maxMembersPerGroup", fontWeight = FontWeight.Bold)
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
    maxMembers: Int,
    onCardClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var adminName by remember { mutableStateOf("Loading...") }
    val isMember = currentUserId != null && group.members.contains(currentUserId)
    val isFull = group.members.size >= maxMembers
    val isCreator = group.creatorId == currentUserId

    LaunchedEffect(group.creatorId) {
        if (group.creatorId.isNotEmpty()) {
            db.collection("users").document(group.creatorId).get()
                .addOnSuccessListener { doc ->
                    adminName = doc.getString("name") ?: "Unknown Admin"
                }
                .addOnFailureListener {
                    adminName = "Unknown Admin"
                }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star, 
                            contentDescription = null, 
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Admin: $adminName",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    color = if (isFull) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${group.members.size} / $maxMembers",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFull) Color(0xFFC62828) else Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = group.subject,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = group.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isMember) {
                Button(
                    onClick = onJoinClick,
                    enabled = !isFull,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Text(if (isFull) "GROUP FULL" else "JOIN GROUP", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = { /* Already joined */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    enabled = false,
                    colors = ButtonDefaults.outlinedButtonColors(
                        disabledContentColor = if (isCreator) Color(0xFFFFA000) else PrimaryBlue
                    )
                ) {
                    Text(if (isCreator) "YOU ARE THE ADMIN" else "ALREADY JOINED", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
