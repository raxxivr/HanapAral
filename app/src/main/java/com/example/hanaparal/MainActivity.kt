package com.example.hanaparal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.hanaparal.navigation.SetupNavGraph
import com.example.hanaparal.ui.theme.HanapAralTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HanapAralTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Set up the navigation graph to manage screen transitions
                    SetupNavGraph(
                        navController = navController,
                        // Apply padding to avoid overlapping with system bars
                        // modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
