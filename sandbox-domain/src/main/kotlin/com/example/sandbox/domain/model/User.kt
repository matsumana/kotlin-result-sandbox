package com.example.sandbox.domain.model

import com.example.sandbox.domain.valueobject.Position

interface User {
    val id: Int
    val name: String
    val position: Position

    fun copy(
        name: String,
        position: Position
    ): User {
        return UserData(id, name, position)
    }

    companion object {
        const val UNGENERATED_ID = -1

        fun create(
            name: String,
            position: Position
        ): User {
            return create(UNGENERATED_ID, name, position)
        }

        fun create(
            id: Int,
            name: String,
            position: Position
        ): User {
            return UserData(id, name, position)
        }
    }
}

private data class UserData(
    override val id: Int,
    override val name: String,
    override val position: Position
) : User
