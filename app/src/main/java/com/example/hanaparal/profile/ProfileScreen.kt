package com.example.hanaparal.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

private val Primary = Color(0xFF1565C0)
private val Background = Color(0xFFF5F7FF)

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

    Scaffold(
        containerColor = Background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Profile Screen", fontSize = 20.sp)
        }
    }
}