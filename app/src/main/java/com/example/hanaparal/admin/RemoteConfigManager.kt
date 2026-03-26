package com.example.hanaparal.admin

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class AppConfig(
    val isGroupCreationEnabled: Boolean = true,
    val announcementHeader: String = "Welcome to HanapAral!",
    val maxMembersPerGroup: Int = 10,
    val isAdminPanelEnabled: Boolean = false
)

class RemoteConfigManager {

    private val remoteConfig = Firebase.remoteConfig
    private val _config = MutableStateFlow(AppConfig())
    val config: StateFlow<AppConfig> = _config.asStateFlow()

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 // 1 hour for production
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        val defaults = mapOf(
            "is_group_creation_enabled" to true,
            "announcement_header" to "Welcome to HanapAral!",
            "max_members_per_group" to 10,
            "admin_panel_enabled" to false
        )
        remoteConfig.setDefaultsAsync(defaults)

        fetchAndActivate()
        setupRealTimeUpdates()
    }

    private fun fetchAndActivate() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("RemoteConfig", "Config params updated: ${task.result}")
                    updateConfigState()
                } else {
                    Log.e("RemoteConfig", "Fetch failed")
                    // Fallback handled by using cached values in updateConfigState
                    updateConfigState()
                }
            }
    }

    private fun setupRealTimeUpdates() {
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Log.d("RemoteConfig", "Updated keys: ${configUpdate.updatedKeys}")
                remoteConfig.activate().addOnCompleteListener {
                    updateConfigState()
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.w("RemoteConfig", "Config update error with code: ${error.code}", error)
            }
        })
    }

    private fun updateConfigState() {
        _config.value = AppConfig(
            isGroupCreationEnabled = remoteConfig.getBoolean("is_group_creation_enabled"),
            announcementHeader = remoteConfig.getString("announcement_header"),
            maxMembersPerGroup = remoteConfig.getLong("max_members_per_group").toInt(),
            isAdminPanelEnabled = remoteConfig.getBoolean("admin_panel_enabled")
        )
    }

    suspend fun forceRefresh(): Boolean {
        return try {
            val result = remoteConfig.fetchAndActivate().await()
            if (result) {
                updateConfigState()
            }
            result
        } catch (e: Exception) {
            Log.e("RemoteConfig", "Force refresh failed", e)
            false
        }
    }
}
