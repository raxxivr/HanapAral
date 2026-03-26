package com.example.hanaparal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hanaparal.admin.AppConfig
import com.example.hanaparal.admin.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoteConfigViewModel @Inject constructor(
    private val remoteConfigManager: RemoteConfigManager
) : ViewModel() {

    // Fixed: Using remoteConfigManager.config directly
    val config: StateFlow<AppConfig> = remoteConfigManager.config
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppConfig()
        )

    private val _isSuperuserAuthenticated = MutableStateFlow(false)
    val isSuperuserAuthenticated: StateFlow<Boolean> = _isSuperuserAuthenticated.asStateFlow()

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    fun showAuthenticationDialog() {
        _showAuthDialog.value = true
    }

    fun hideAuthenticationDialog() {
        _showAuthDialog.value = false
    }

    fun setSuperuserAuthenticated(authenticated: Boolean) {
        _isSuperuserAuthenticated.value = authenticated
        if (authenticated) {
            refreshConfig()
        }
    }

    fun logoutSuperuser() {
        _isSuperuserAuthenticated.value = false
    }

    fun refreshConfig() {
        viewModelScope.launch {
            remoteConfigManager.forceRefresh()
        }
    }
}