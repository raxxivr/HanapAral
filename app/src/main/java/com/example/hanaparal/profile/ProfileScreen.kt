package com.example.hanaparal.profile

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val Primary = Color(0xFF1565C0)
private val Background = Color(0xFFF5F7FF)
private val TextPrimary = Color(0xFF1A237E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var name by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user?.email ?: "") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(user?.uid) {
        loadProfile(user?.uid, db) { loadedUser ->
            loadedUser?.let {
                name = it.name
                course = it.course
                email = it.email
            }
            isLoading = false
        }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            message = null
        }
    }

    fun saveProfile() {
        val uid = user?.uid ?: return

        if (name.isBlank() || course.isBlank()) {
            message = "Please fill all required fields"
            return
        }

        isSaving = true

        val newUser = User(uid, name.trim(), course.trim(), email.trim())

        db.collection("users").document(uid)
            .set(newUser.toMap())
            .addOnSuccessListener {
                isSaving = false
                message = "Profile saved successfully!"
            }
            .addOnFailureListener {
                isSaving = false
                message = "Failed to save profile"
            }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            ProfileHeader(name)

            Text("Student Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Primary)

            ProfileTextField(
                value = name,
                onChange = { name = it },
                label = "Full Name",
                icon = Icons.Default.AccountCircle
            )

            ProfileTextField(
                value = course,
                onChange = { course = it },
                label = "Course",
                icon = Icons.Default.Star
            )

            ProfileTextField(
                value = email,
                onChange = { email = it },
                label = "Email",
                icon = Icons.Default.MailOutline,
                enabled = user?.email.isNullOrEmpty()
            )

            SaveButton(isSaving) { saveProfile() }
        }
    }
}

@Composable
fun ProfileHeader(name: String) {
    val initials = name.take(2).uppercase().ifEmpty { "?" }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
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
        leadingIcon = { Icon(icon, contentDescription = label, tint = Primary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        enabled = enabled
    )
}

@Composable
fun SaveButton(isSaving: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isSaving,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary)
    ) {
        if (isSaving) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Saving...")
        } else {
            Text("Save Profile")
        }
    }
}


fun loadProfile(
    uid: String?,
    db: FirebaseFirestore,
    onResult: (User?) -> Unit
) {
    uid ?: return onResult(null)

    db.collection("users").document(uid)
        .get()
        .addOnSuccessListener {
            onResult(it.toObject(User::class.java))
        }
        .addOnFailureListener {
            onResult(null)
        }
}



