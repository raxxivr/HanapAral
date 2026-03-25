package com.example.hanaparal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.hanaparal.auth.AuthManager
import com.example.hanaparal.navigation.Screen
import com.example.hanaparal.navigation.SetupNavGraph
import com.example.hanaparal.ui.theme.HanapAralTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val authManager = AuthManager(this)

        enableEdgeToEdge()
        setContent {
            HanapAralTheme {
                val navController = rememberNavController()
                
                // Ino-observe natin ang currentUser flow
                val user by authManager.currentUser.collectAsState()
                
                // State para malaman kung tapos na ba mag-check ng initial session
                var isCheckingSession by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    // Bigyan natin ng konting delay para masiguradong nabasa ang Firebase session
                    // o diretso na tayo dahil ang AuthManager init ay reactive na.
                    isCheckingSession = false
                }

                if (isCheckingSession) {
                    // Habang chine-check ang session, loading muna
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Kapag may user, GroupList ang start. Kapag wala, Login.
                    val startDestination = if (user != null) Screen.GroupList.route else Screen.Login.route
                    
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        SetupNavGraph(
                            navController = navController,
                            authManager = authManager,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }
}
