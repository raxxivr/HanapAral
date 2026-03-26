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
import com.example.hanaparal.models.User


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

    fun saveProfile() {
        val uid = user?.uid ?: return

        if (name.isBlank() || course.isBlank()) return

        isSaving = true

        val newUser = User(uid, name, course, email)

        db.collection("users").document(uid)
            .set(newUser.toMap())
            .addOnSuccessListener { isSaving = false }
            .addOnFailureListener { isSaving = false }
    }

    Scaffold {}
}