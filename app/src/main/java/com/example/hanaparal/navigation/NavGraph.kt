package com.example.hanaparal.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hanaparal.admin.RemoteConfigManager
import com.example.hanaparal.groups.CreateGroupScreen
import com.example.hanaparal.groups.GroupListScreen
import com.example.hanaparal.profile.ProfileScreen
import com.example.hanaparal.ui.admin.AdminScreen
import com.example.hanaparal.viewmodel.RemoteConfigViewModel
import com.example.hanaparal.groups.CreateGroupScreen
import com.example.hanaparal.groups.GroupListScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    remoteConfigManager: RemoteConfigManager,
    modifier: Modifier = Modifier
) {
    val config by remoteConfigManager.config.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(route = Screen.Login.route) {
            // Simple button to go to profile for navigation testing
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Button(onClick = { navController.navigate(Screen.Profile.route) }) {
                    Text(text = "Go to Profile")
                }
            }
        }
        
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                isAdminPanelEnabled = config.isAdminPanelEnabled,
                onAdminClick = { navController.navigate(Screen.Admin.route) },
                onNavigateToGroups = { navController.navigate(Screen.GroupList.route) }
            )
        }

        composable(route = Screen.Admin.route) {
            val viewModel: RemoteConfigViewModel = hiltViewModel()
            AdminScreen(
                remoteConfigViewModel = viewModel,
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable(route = Screen.GroupList.route) {
            GroupListScreen(
                isGroupCreationEnabled = config.isGroupCreationEnabled,
                maxMembersPerGroup = config.maxMembersPerGroup,
                onCreateGroupClick = {
                    if (config.isGroupCreationEnabled) {
                        navController.navigate(Screen.CreateGroup.route)
                    }
                }
            )
        }

        composable(route = Screen.CreateGroup.route) {
            if (config.isGroupCreationEnabled) {
                CreateGroupScreen(
                    maxMembers = config.maxMembersPerGroup,
                    onBack = { navController.popBackStack() }
                )
            } else {
                // If disabled, go back
                navController.popBackStack()
            }
        }
    }
}
