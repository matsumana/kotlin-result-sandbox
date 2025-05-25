package com.example.sandbox.usecase

import com.example.sandbox.domain.model.User
import com.example.sandbox.dto.UserCreateRequestDto
import com.example.sandbox.dto.UserUpdateRequestDto
import com.example.sandbox.repository.UserRepository
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.flatMap
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
    }

    sealed class UpdateResult {
        data class NotFoundError(val message: String) : UpdateResult()
        data class EnumConvertError(val message: String) : UpdateResult()
    }

    fun findById(id: Int): Result<User, FindByIdResult> =
        userRepository.findById(id)
            .mapError { FindByIdResult.NotFoundError(it.message) }

    @Transactional
    fun create(request: UserCreateRequestDto): Result<User, CreateResult> =
        User.create(
            name = request.name,
            position = request.position
        ).mapError { err ->
            when (err) {
                is User.CreateResult.EnumConvertError -> CreateResult.EnumConvertError(err.message)
            }
        }.andThen { user ->
            userRepository.create(user)

            // The `create` function updates the `user` with an auto-generated ID.
            Ok(user)
        }

    @Transactional
    fun update(id: Int, request: UserUpdateRequestDto): Result<Int, UpdateResult> =
        userRepository.findById(
            id
        ).mapError {
            UpdateResult.NotFoundError(it.message)
        }.flatMap { existingUser ->
            existingUser.copy(
                name = request.name,
                position = request.position
            ).mapError { err ->
                when (err) {
                    is User.CopyResult.EnumConvertError -> UpdateResult.EnumConvertError(err.message)
                }
            }
        }.andThen { user ->
            Ok(userRepository.update(user))
        }
}
