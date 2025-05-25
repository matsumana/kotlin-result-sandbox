package com.example.sandbox.repository

import com.example.sandbox.error.common.NotFoundError
import com.example.sandbox.mapper.UserMapper
import com.example.sandbox.record.User
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val userMapper: UserMapper
) : UserRepository {

    override fun findById(id: Int): Result<User, NotFoundError> =
        userMapper.findById(id)
            ?.let { Ok(it) }
            ?: Err(
                NotFoundError("unknown user with id $id")
            )

    override fun create(user: User): Int = userMapper.create(user)

    override fun update(updatedUser: User): Int = userMapper.update(updatedUser)
}
