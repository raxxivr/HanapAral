package com.example.hanaparal.admin

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class RemoteConfigManager {
    private val remoteConfig = Firebase.remoteConfig

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        // Default values
        remoteConfig.setDefaultsAsync(mapOf(
            "is_group_creation_enabled" to true,
            "announcement_header" to "Welcome to HanapAral!",
            "max_members_per_group" to 10
        ))
    }

    fun fetchAndActivate(onComplete: (Boolean) -> Unit) {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    fun isGroupCreationEnabled(): Boolean = remoteConfig.getBoolean("is_group_creation_enabled")
    fun getAnnouncementHeader(): String = remoteConfig.getString("announcement_header")
    fun getMaxMembers(): Long = remoteConfig.getLong("max_members_per_group")
}
