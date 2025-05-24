package com.example.sandbox.controller.dto

import com.example.sandbox.valueobject.Position

data class UserCreateRequest(
    val name: String,

    // TODO : change to String
    val position: Position
)
