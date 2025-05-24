package com.example.sandbox.usecase

import com.example.sandbox.record.User
import com.example.sandbox.repository.UserRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserUseCase(
    private val employeeRepository: UserRepository
) {
    sealed class FindByIdResult {
        data class NotFound(val message: String) : FindByIdResult()
    }

    fun findById(id: Int): Result<User, FindByIdResult> =
        employeeRepository.findById(id)
            ?.let { Ok(it) }
            ?: Err(
                FindByIdResult.NotFound("unknown user with id $id")
            )

    @Transactional
    fun create(user: User): Int {
        return employeeRepository.create(user)
    }
}
