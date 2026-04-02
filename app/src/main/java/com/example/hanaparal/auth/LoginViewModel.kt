package com.example.hanaparal.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hanaparal.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val signInResult = repository.signInWithGoogle(context)
            signInResult.fold(
                onSuccess = { idToken ->
                    val firebaseResult = repository.firebaseAuthWithGoogle(idToken)
                    _authState.value = firebaseResult.fold(
                        onSuccess = { AuthState.Success(it) },
                        onFailure = { AuthState.Error(it.message ?: "Firebase Auth failed") }
                    )
                },
                onFailure = { 
                    _authState.value = AuthState.Error(it.message ?: "Google Sign-In failed") 
                }
            )
        }
    }
}
