package com.example.hanaparal.models

data class StudyGroup(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val subject: String = "",
    val creatorId: String = "",
    val members: List<String> = emptyList()
)
