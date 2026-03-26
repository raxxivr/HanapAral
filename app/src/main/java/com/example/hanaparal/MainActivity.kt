package com.example.hanaparal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.hanaparal.admin.RemoteConfigManager
import com.example.hanaparal.navigation.SetupNavGraph
import com.example.hanaparal.ui.theme.HanapAralTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var remoteConfigManager: RemoteConfigManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize RemoteConfigManager
        remoteConfigManager = RemoteConfigManager()
        
        enableEdgeToEdge()
        setContent {
            HanapAralTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Set up the navigation graph and pass RemoteConfigManager
                    SetupNavGraph(
                        navController = navController,
                        remoteConfigManager = remoteConfigManager
                    )
                }
            }
        }
    }
}
