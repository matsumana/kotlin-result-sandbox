package com.example.sandbox.repository

import com.example.sandbox.mapper.UserMapper
import com.example.sandbox.record.User
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.springframework.stereotype.Component

@Component
class UserRepository(
    private val userMapper: UserMapper
) {
    data class NotFoundError(val message: String)

    fun findById(id: Int): Result<User, NotFoundError> =
        userMapper.findById(id)
            ?.let { Ok(it) }
            ?: Err(
                NotFoundError("unknown user with id $id")
            )

    fun create(user: User): Int = userMapper.create(user)

    fun update(updatedUser: User): Int = userMapper.update(updatedUser)
}
