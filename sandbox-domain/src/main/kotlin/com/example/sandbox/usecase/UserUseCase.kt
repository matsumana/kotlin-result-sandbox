package com.example.sandbox.usecase

import com.example.sandbox.dto.UserCreateRequestDto
import com.example.sandbox.dto.UserUpdateRequestDto
import com.example.sandbox.record.User
import com.example.sandbox.repository.UserRepository
import com.example.sandbox.valueobject.Position
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.zip
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
        Position.of(request.position)
            .mapError { CreateResult.EnumConvertError(it.message) }
            .andThen { position ->
                val user = User(
                    id = -1, // auto-generated
                    request.name,
                    position
                )

                userRepository.create(user)

                // The `create` function updates the `user` with an auto-generated ID.
                Ok(user)
            }

    @Transactional
    fun update(id: Int, request: UserUpdateRequestDto): Result<Int, UpdateResult> =
        zip(
            {
                userRepository.findById(id)
                    .mapError { UpdateResult.NotFoundError(it.message) }
            },
            {
                Position.of(request.position)
                    .mapError { UpdateResult.EnumConvertError(it.message) }
            }
        ) { existingUser, position ->
            val user = existingUser.copy(
                name = request.name,
                position = position
            )

            userRepository.update(user)
        }
}
