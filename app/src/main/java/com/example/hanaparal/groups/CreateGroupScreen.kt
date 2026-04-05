package com.example.hanaparal.groups

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.models.StudyGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val PrimaryBlue = Color(0xFF1565C0)
private val BackgroundGray = Color(0xFFF8F9FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    maxMembers: Int = 10,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CREATE GROUP", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
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
                    Text("Group Details", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = PrimaryBlue)
                    
                    Divider(color = Color.LightGray, thickness = 1.dp)

                    CreateTextField(name, { name = it }, "Group Name", Icons.Default.Groups)
                    CreateTextField(subject, { subject = it }, "Subject", Icons.Default.Book)
                    CreateTextField(description, { description = it }, "Description", Icons.Default.Info, singleLine = false)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Member Limit: $maxMembers members",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoading) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        }
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
                                            Toast.makeText(context, "Group Created Successfully!", Toast.LENGTH_SHORT).show()
                                            onBack()
                                        }
                                        .addOnFailureListener {
                                            isLoading = false
                                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(context, "Please fill in Group Name and Subject", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Create Group", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateTextField(value: String, onChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, singleLine: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = PrimaryBlue)
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedLabelColor = PrimaryBlue,
            unfocusedLabelColor = Color.Gray
        )
    )
}
