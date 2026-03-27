package com.example.hanaparal.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hanaparal.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                val user = doc.toObject(User::class.java)
                if (user != null) {
                    _profileState.value = ProfileState.Success(user)
                } else {
                    _profileState.value = ProfileState.Error("Profile not found")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun updateProfile(name: String, course: String, year: String = "") {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val updateData = mutableMapOf<String, Any>(
                    "name" to name,
                    "course" to course
                )
                if (year.isNotEmpty()) {
                    updateData["year"] = year
                }
                
                firestore.collection("users").document(uid).update(updateData).await()
                loadProfile()
            } catch (e: Exception) {
                // If update fails because document doesn't have fields yet, try set
                try {
                    val user = User(
                        uid = uid, 
                        name = name, 
                        course = course, 
                        year = year, 
                        email = auth.currentUser?.email ?: ""
                    )
                    firestore.collection("users").document(uid).set(user).await()
                    _profileState.value = ProfileState.Success(user)
                } catch (e2: Exception) {
                    _profileState.value = ProfileState.Error(e2.message ?: "Update failed")
                }
            }
        }
    }
}
