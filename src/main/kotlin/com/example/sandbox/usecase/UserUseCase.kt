package com.example.sandbox.usecase

import com.example.sandbox.controller.dto.UserCreateRequest
import com.example.sandbox.controller.dto.UserUpdateRequest
import com.example.sandbox.record.User
import com.example.sandbox.repository.UserRepository
import com.example.sandbox.valueobject.Position
import com.github.michaelbull.result.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserUseCase(
    private val employeeRepository: UserRepository
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
        employeeRepository.findById(id)
            ?.let { Ok(it) }
            ?: Err(
                FindByIdResult.NotFoundError("unknown user with id $id")
            )

    @Transactional
    fun create(request: UserCreateRequest): Result<User, CreateResult> =
        Position.of(request.position)
            .mapError { CreateResult.EnumConvertError(it.message) }
            .andThen { position ->
                val user = User(
                    id = -1, // auto-generated
                    request.name,
                    position
                )

                employeeRepository.create(user)

                // The `create` function updates the `user` with an auto-generated ID.
                Ok(user)
            }

    @Transactional
    fun update(id: Int, request: UserUpdateRequest): Result<Int, UpdateResult> {
        val existingUser = employeeRepository.findById(id)
            ?: return Err(
                UpdateResult.NotFoundError("User with id $id does not exist")
            )

        return Position.of(request.position)
            .mapError { UpdateResult.EnumConvertError(it.message) }
            .andThen { position ->
                val user = existingUser.copy(
                    name = request.name,
                    position = position
                )

                val updatedRowsCount = employeeRepository.update(user)

                Ok(updatedRowsCount)
            }
    }
}
