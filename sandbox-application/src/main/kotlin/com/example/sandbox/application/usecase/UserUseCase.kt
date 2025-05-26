package com.example.sandbox.application.usecase

import com.example.sandbox.application.dto.UserCreateRequestDto
import com.example.sandbox.application.dto.UserResponseDto
import com.example.sandbox.application.dto.UserUpdateRequestDto
import com.example.sandbox.domain.model.User
import com.example.sandbox.domain.repository.UserRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.mapError
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserUseCase(
    private val userRepository: UserRepository,
) {
    sealed class FindByIdResult {
        data class NotFoundError(val message: String) : FindByIdResult()
    }

    sealed class CreateResult {
        data class EnumConvertError(val message: String) : CreateResult()
        data object InvalidMailAddressError : CreateResult()
    }

    sealed class UpdateResult {
        data class NotFoundError(val message: String) : UpdateResult()
        data class EnumConvertError(val message: String) : UpdateResult()
        data object InvalidMailAddressError : UpdateResult()
    }

    fun findById(id: Int): Result<UserResponseDto, FindByIdResult> = binding {
        val found = userRepository.findById(id)
            .mapError { FindByIdResult.NotFoundError(it.message) }
            .bind()

        UserResponseDto(
            id = found.id,
            name = found.name,
            position = found.position.toString(),
            mailAddress = found.mailAddress.value
        )
    }

    @Transactional
    fun create(request: UserCreateRequestDto): Result<UserResponseDto, CreateResult> = binding {
        val user = User.create(
            name = request.name,
            position = request.position,
            mailAddress = request.mailAddress
        ).mapError { err ->
            when (err) {
                is User.CreateResult.EnumConvertError -> CreateResult.EnumConvertError(err.message)
                is User.CreateResult.InvalidMailAddressError -> CreateResult.InvalidMailAddressError
            }
        }.bind()

        userRepository.create(user)

        // The `create` function updates the `user` with an auto-generated ID.
        UserResponseDto(
            id = user.id,
            name = user.name,
            position = user.position.toString(),
            mailAddress = user.mailAddress.value
        )
    }

    @Transactional
    fun update(id: Int, request: UserUpdateRequestDto): Result<Int, UpdateResult> = binding {
        val existingUser = userRepository.findById(
            id
        ).mapError { err ->
            UpdateResult.NotFoundError(err.message)
        }.bind()

        val copiedUser = existingUser.copy(
            name = request.name,
            position = request.position,
            mailAddress = request.mailAddress
        ).mapError { err ->
            when (err) {
                is User.CopyResult.EnumConvertError -> UpdateResult.EnumConvertError(err.message)
                is User.CopyResult.InvalidMailAddressError -> UpdateResult.InvalidMailAddressError
            }
        }.bind()

        userRepository.update(copiedUser)
    }
}
