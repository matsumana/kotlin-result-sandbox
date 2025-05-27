package com.example.sandbox.domain.model

import com.example.sandbox.domain.valueobject.MailAddress
import com.example.sandbox.domain.valueobject.Position
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.mapError
import de.huxhorn.sulky.ulid.ULID

interface User {
    val id: ULID.Value
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
                id = generateId(),
                name = name,
                position = convertedPosition,
                mailAddress = convertedMailAddress
            )
        }

        private fun generateId(): ULID.Value = ULID().nextValue()
    }
}

private data class UserData(
    override val id: ULID.Value,
    override val name: String,
    override val position: Position,
    override val mailAddress: MailAddress
) : User
