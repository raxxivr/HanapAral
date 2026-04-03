package com.example.hanaparal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.example.hanaparal.admin.RemoteConfigManager
import com.example.hanaparal.navigation.SetupNavGraph
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var remoteConfigManager: RemoteConfigManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            HanapAralTheme {
                val navController = rememberNavController()
                
                // Request Notification Permission for Android 13+
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    
                    // Update FCM Token in Firestore
                    updateFcmToken()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SetupNavGraph(
                        navController = navController,
                        remoteConfigManager = remoteConfigManager,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun updateFcmToken() {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return
        
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                FirebaseFirestore.getInstance().collection("users")
                    .document(userId)
                    .update("fcmToken", token)
            }
        }
    }
}
