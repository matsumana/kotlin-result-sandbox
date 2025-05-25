package com.example.sandbox.domain.model

import com.example.sandbox.domain.valueobject.Position
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapError

interface User {
    val id: Int
    val name: String
    val position: Position

    sealed class CreateResult {
        data class EnumConvertError(val message: String) : CreateResult()
    }

    sealed class CopyResult {
        data class EnumConvertError(val message: String) : CopyResult()
    }

    fun copy(
        name: String,
        position: String
    ): Result<User, CopyResult> =
        Position.of(position)
            .mapError { CopyResult.EnumConvertError(it.message) }
            .andThen { convertedPosition ->
                Ok(
                    UserData(
                        id, // keep the original id
                        name,
                        convertedPosition
                    )
                )
            }

    companion object {
        const val UNGENERATED_ID = -1

        fun create(
            name: String,
            position: String
        ): Result<User, CreateResult> =
            create(
                UNGENERATED_ID, // will be replaced with an auto-generated ID after creation with the repository
                name,
                position
            )

        fun create(
            id: Int,
            name: String,
            position: String
        ): Result<User, CreateResult> =
            Position.of(position)
                .mapError { CreateResult.EnumConvertError(it.message) }
                .andThen { convertedPosition ->
                    Ok(UserData(id, name, convertedPosition))
                }
    }
}

private data class UserData(
    override val id: Int,
    override val name: String,
    override val position: Position
) : User
