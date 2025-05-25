package com.example.sandbox.record

import com.example.sandbox.valueobject.Position

data class User(
    val id: Int,
    val name: String,
    val position: Position
)
