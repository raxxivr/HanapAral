package com.example.hanaparal.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hanaparal.groups.CreateGroupScreen
import com.example.hanaparal.groups.GroupListScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(route = Screen.Login.route) {
            // Member 1: Implement LoginScreen here
            // Temporary button to navigate to GroupList for testing
            androidx.compose.material3.Button(onClick = { navController.navigate(Screen.GroupList.route) }) {
                Text(text = "Go to Group List")
            }
        }
        composable(route = Screen.Profile.route) {
            // Member 2: Implement ProfileScreen here
            Text(text = "Profile Screen Placeholder")
        }
        composable(route = Screen.GroupList.route) {
            GroupListScreen(
                onCreateGroupClick = {
                    navController.navigate(Screen.CreateGroup.route)
                }
            )
        }
        composable(route = Screen.CreateGroup.route) {
            CreateGroupScreen(
                onGroupCreated = {
                    navController.popBackStack()
                }
            )
        }
    }
}
