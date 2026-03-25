package com.example.hanaparal.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Profile : Screen("profile")
    object GroupList : Screen("group_list")
    object CreateGroup : Screen("create_group")
}
