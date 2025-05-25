package com.example.sandbox.record

import com.example.sandbox.domain.model.User
import com.example.sandbox.domain.valueobject.Position

data class UserData(
    override val id: Int,
    override val name: String,
    override val position: Position
) : User
