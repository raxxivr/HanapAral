package com.example.hanaparal.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hanaparal.auth.AuthManager
import com.example.hanaparal.auth.LoginScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    authManager: AuthManager,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                authManager = authManager,
                onLoginSuccess = {
                    navController.navigate(Screen.GroupList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.Profile.route) {
            // Member 2: Implement ProfileScreen here
            Text(text = "Profile Screen Placeholder")
        }
        composable(route = Screen.GroupList.route) {
            // Member 3: Implement GroupListScreen here
            Text(text = "Group List Screen Placeholder")
        }
        composable(route = Screen.CreateGroup.route) {
            // Member 3: Implement CreateGroupScreen here
            Text(text = "Create Group Screen Placeholder")
        }
    }
}
