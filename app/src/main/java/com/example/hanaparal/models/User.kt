package com.example.hanaparal.models

data class User(
    val uid: String = "",
    val name: String = "",
    val course: String = "",
    val email: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "name" to name,
        "course" to course,
        "email" to email
    )
}