package com.example.hanaparal.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hanaparal.admin.RemoteConfigManager
import com.example.hanaparal.auth.LoginScreen
import com.example.hanaparal.groups.CreateGroupScreen
import com.example.hanaparal.groups.GroupListScreen
import com.example.hanaparal.profile.ProfileScreen
import com.example.hanaparal.profile.ProfileSetupScreen
import com.example.hanaparal.ui.admin.AdminScreen
import com.example.hanaparal.viewmodel.RemoteConfigViewModel

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
            LoginScreen(
                onLoginSuccess = { isNewUser ->
                    if (isNewUser) {
                        navController.navigate(Screen.ProfileSetup.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.GroupList.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(route = Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.GroupList.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                isAdminPanelEnabled = config.isAdminPanelEnabled,
                onAdminClick = { navController.navigate(Screen.Admin.route) },
                onNavigateToGroups = { navController.navigate(Screen.GroupList.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
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
                    navController.navigate(Screen.CreateGroup.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(route = Screen.CreateGroup.route) {
            CreateGroupScreen(
                maxMembers = config.maxMembersPerGroup,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
