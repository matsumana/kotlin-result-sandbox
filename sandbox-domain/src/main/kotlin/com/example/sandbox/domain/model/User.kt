package com.example.sandbox.domain.model

import com.example.sandbox.domain.valueobject.Position

data class User(
    val id: Int,
    val name: String,
    val position: Position
)
