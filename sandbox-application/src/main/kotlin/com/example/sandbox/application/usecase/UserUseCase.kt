package com.example.sandbox.application.usecase

import com.example.sandbox.application.dto.UserCreateRequestDto
import com.example.sandbox.application.dto.UserResponseDto
import com.example.sandbox.application.dto.UserUpdateRequestDto
import com.example.sandbox.application.helper.TransactionHelper
import com.example.sandbox.domain.extension.parseULID
import com.example.sandbox.domain.model.User
import com.example.sandbox.domain.repository.UserRepository
import com.example.sandbox.domain.valueobject.MailAddress
import com.example.sandbox.domain.valueobject.Position
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.mapError
import org.springframework.stereotype.Service

@Service
class UserUseCase(
    private val transactionHelper: TransactionHelper,
    private val userRepository: UserRepository,
) {
    sealed interface FindByIdError {
        data class InvalidULIDError(val message: String) : FindByIdError
        data class NotFoundError(val message: String) : FindByIdError
    }

    sealed interface CreateError {
        data class ExceptionOccurredError(val exception: Exception) : CreateError
        data class EnumConvertError(val message: String) : CreateError
        data object InvalidMailAddressError : CreateError
    }

    sealed interface UpdateError {
        data class ExceptionOccurredError(val exception: Exception) : UpdateError
        data class InvalidULIDError(val message: String) : UpdateError
        data class NotFoundError(val message: String) : UpdateError
        data class EnumConvertError(val message: String) : UpdateError
        data object InvalidMailAddressError : UpdateError
    }

    fun findById(id: String): Result<UserResponseDto, FindByIdError> = binding {
        val parsedId = id.parseULID().mapError { err ->
            FindByIdError.InvalidULIDError(err.message)
        }.bind()

        val found = userRepository.findById(
            parsedId
        ).mapError {
            FindByIdError.NotFoundError(it.message)
        }.bind()

        UserResponseDto(
            id = found.id.toString(),
            name = found.name,
            position = found.position.toString(),
            mailAddress = found.mailAddress.value
        )
    }

    fun create(
        request: UserCreateRequestDto
    ): Result<UserResponseDto, CreateError> = transactionHelper.withExceptionMapper<CreateError> {
        CreateError.ExceptionOccurredError(it)
    }.binding {
        val position = Position.of(request.position)
            .mapError { CreateError.EnumConvertError(it.message) }
            .bind()

        val mailAddress = MailAddress.of(request.mailAddress)
            .mapError { CreateError.InvalidMailAddressError }
            .bind()

        val user = User.create(
            name = request.name,
            position = position,
            mailAddress = mailAddress
        )

        userRepository.create(user)

        UserResponseDto(
            id = user.id.toString(),
            name = user.name,
            position = user.position.toString(),
            mailAddress = user.mailAddress.value
        )
    }

    fun update(
        id: String,
        request: UserUpdateRequestDto
    ): Result<Int, UpdateError> = transactionHelper.withExceptionMapper<UpdateError> {
        UpdateError.ExceptionOccurredError(it)
    }.binding {
        val parsedId = id.parseULID().mapError { err ->
            UpdateError.InvalidULIDError(err.message)
        }.bind()

        val existingUser = userRepository.findById(
            parsedId
        ).mapError { err ->
            UpdateError.NotFoundError(err.message)
        }.bind()

        val position = Position.of(request.position)
            .mapError { UpdateError.EnumConvertError(it.message) }
            .bind()

        val mailAddress = MailAddress.of(request.mailAddress)
            .mapError { UpdateError.InvalidMailAddressError }
            .bind()

        val updatedUser = existingUser.changeProfile(
            name = request.name,
            position = position,
            mailAddress = mailAddress
        )

        userRepository.update(updatedUser)
            .mapError { err ->
                UpdateError.NotFoundError(err.message)
            }.bind()
    }
}
