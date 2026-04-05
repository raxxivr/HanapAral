package com.example.hanaparal.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PrimaryBlue = Color(0xFF1565C0)
private val BackgroundGray = Color(0xFFF8F9FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isAdminPanelEnabled: Boolean = false,
    onAdminClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val data = doc.toObject(User::class.java)
                    if (data != null) {
                        name = data.name
                        year = data.year
                        email = currentUser?.email ?: data.email
                        
                        // Auto-correct swapped data from Firestore
                        if (data.course.contains("@") && !data.email.contains("@")) {
                            course = data.email
                        } else if (data.course.contains("@")) {
                            course = "" // Clear if it's just the email again
                        } else {
                            course = data.course
                        }
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } ?: run {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = BackgroundGray,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("MY PROFILE", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
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
                    ),
                    actions = {
                        IconButton(onClick = {
                            if (isEditing) {
                                isSaving = true
                                val userData = User(
                                    uid = currentUser?.uid ?: "",
                                    name = name,
                                    course = course,
                                    email = email,
                                    year = year
                                )

                                db.collection("users").document(currentUser?.uid ?: "").set(userData)
                                    .addOnSuccessListener {
                                        isSaving = false
                                        isEditing = false
                                    }
                                    .addOnFailureListener {
                                        isSaving = false
                                    }
                            } else {
                                isEditing = true
                            }
                        }) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { padding ->
            if (isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    ProfileHeader(name)

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text("Student Information", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = PrimaryBlue)

                            if (isEditing) {
                                Text(
                                    text = "Editing Mode",
                                    color = Color(0xFFD32F2F),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            HorizontalDivider(color = Color.LightGray, thickness = 1.dp)

                            ProfileTextField(name, { name = it }, "Full Name", Icons.Default.Person, isEditing)
                            ProfileTextField(course, { course = it }, "Course", Icons.Default.School, isEditing)
                            ProfileTextField(year, { year = it }, "Year Level", Icons.Default.Info, isEditing)
                            ProfileTextField(email, { email = it }, "Email Address", Icons.Default.Email, false)

                            Spacer(modifier = Modifier.height(8.dp))

                            if (isAdminPanelEnabled) {
                                Button(
                                    onClick = onAdminClick,
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Admin Panel", fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isLoggingOut = true
                                        delay(1500)
                                        auth.signOut()
                                        onLogout()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Sign Out", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        if (isLoggingOut) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Signing out...", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(name: String) {
    val initials = if (name.isNotBlank()) name.take(2).uppercase() else "?"
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(Color.White).padding(3.dp)) {
            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(PrimaryBlue), contentAlignment = Alignment.Center) {
                Text(initials, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(text = if (name.isNotBlank()) name else "Student", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProfileTextField(value: String, onChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = PrimaryBlue)
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = Color(0xFFE0E0E0),
            disabledBorderColor = Color(0xFFE0E0E0),
            disabledTextColor = Color.Black,
            disabledLabelColor = Color.Gray
        )
    )
}
