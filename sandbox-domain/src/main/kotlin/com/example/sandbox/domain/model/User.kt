package com.example.sandbox.domain.model

import com.example.sandbox.domain.valueobject.MailAddress
import com.example.sandbox.domain.valueobject.Position
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.mapError

interface User {
    val id: Int
    val name: String
    val position: Position
    val mailAddress: MailAddress

    sealed class CreateResult {
        data class EnumConvertError(val message: String) : CreateResult()
        data object InvalidMailAddressError : CreateResult()
    }

    sealed class CopyResult {
        data class EnumConvertError(val message: String) : CopyResult()
        data object InvalidMailAddressError : CopyResult()
    }

    fun copy(
        name: String = this.name,
        position: String = this.position.toString(),
        mailAddress: String = this.mailAddress.value
    ): Result<User, CopyResult> = binding {
        val convertedPosition = Position.of(position)
            .mapError { CopyResult.EnumConvertError(it.message) }
            .bind()
        val convertedMailAddress = MailAddress.create(mailAddress)
            .mapError { CopyResult.InvalidMailAddressError }
            .bind()

        UserData(
            id = id, // keep the original id
            name = name,
            position = convertedPosition,
            mailAddress = convertedMailAddress,
        )
    }

    companion object {
        const val UNGENERATED_ID = -1

        fun create(
            id: Int = UNGENERATED_ID, // will be replaced with an auto-generated ID after creation with the repository
            name: String,
            position: String,
            mailAddress: String
        ): Result<User, CreateResult> = binding {
            val convertedPosition = Position.of(position)
                .mapError { CreateResult.EnumConvertError(it.message) }
                .bind()
            val convertedMailAddress = MailAddress.create(mailAddress)
                .mapError { CreateResult.InvalidMailAddressError }
                .bind()

            UserData(
                id = id,
                name = name,
                position = convertedPosition,
                mailAddress = convertedMailAddress
            )
        }
    }
}

private data class UserData(
    override val id: Int,
    override val name: String,
    override val position: Position,
    override val mailAddress: MailAddress
) : User
