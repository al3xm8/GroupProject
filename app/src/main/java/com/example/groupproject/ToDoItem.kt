package com.example.groupproject

data class ToDoItem(
    val title: String = "",
    val description: String = "",
    var completed: Boolean = false,
    val username: String = "",
    val isSpecial: Boolean = false,
    val key: String = ""
)
