package com.example.hanaparal.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(route = Screen.Login.route) {
            // Member 1: Implement LoginScreen here
            Text(text = "Login Screen Placeholder")
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
