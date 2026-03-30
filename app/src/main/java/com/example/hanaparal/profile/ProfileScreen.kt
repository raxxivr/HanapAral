package com.example.hanaparal.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Theme Colors
private val PrimaryBlue = Color(0xFF1565C0)
private val BackgroundGray = Color(0xFFF8F9FA)

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
    onAdminClick: () -> Unit = {},
    onNavigateToGroups: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var name by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user?.email ?: "") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // LOAD PROFILE LOGIC
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val data = doc.toObject(User::class.java)
                    if (data != null) {
                        name = data.name
                        course = data.course
                        year = data.year
                        email = data.email
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    message = "Error loading profile"
                }
        } ?: run {
            isLoading = false
        }
    }

    // SNACKBAR HANDLER
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            message = null
        }
    }

    // SAVE PROFILE LOGIC
    fun saveProfile() {
        val uid = user?.uid ?: return
        if (name.isBlank() || course.isBlank()) {
            message = "Please fill all required fields"
            return
        }

        isSaving = true
        val userData = User(
            uid = uid,
            name = name.trim(),
            course = course.trim(),
            year = year.trim(),
            email = email.trim()
        )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onNavigateToGroups) {
                Text(text = "Browse Groups")
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundGray,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MY PROFILE", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                ),
                actions = {
                    if (isAdminPanelEnabled) {
                        IconButton(onClick = onAdminClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Admin", tint = Color.White)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToGroups,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Search, contentDescription = null) },
                text = { Text("Find Groups") }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
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
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Student Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = PrimaryBlue
                        )

                        ProfileTextField(name, { name = it }, "Full Name", Icons.Default.Person)
                        ProfileTextField(course, { course = it }, "Course", Icons.Default.Edit)
                        ProfileTextField(year, { year = it }, "Year Level", Icons.Default.Info)
                        ProfileTextField(email, { email = it }, "Email Address", Icons.Default.Email, false)

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { saveProfile() },
                            enabled = !isSaving,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Save Profile", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Member 2: Add Profile Details Here", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = PrimaryBlue) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = Color.LightGray
        )
    )
}
