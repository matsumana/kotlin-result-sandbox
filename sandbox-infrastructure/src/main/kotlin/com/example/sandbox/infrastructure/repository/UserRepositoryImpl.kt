package com.example.sandbox.infrastructure.repository

import com.example.sandbox.domain.common.NotFoundError
import com.example.sandbox.domain.model.User
import com.example.sandbox.domain.repository.UserRepository
import com.example.sandbox.infrastructure.mapper.UserMapper
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
            ?: Err(NotFoundError("unknown user with id $id"))

    override fun create(user: User): Int = userMapper.create(user)

    override fun update(updatedUser: User): Int = userMapper.update(updatedUser)
}
