package com.flawlesse.notes.data

data class Note (
    val id: Int,
    val title: String,
    val content: String,
    val tags: List<String>,
    val dateCreated: String
)