package com.example.sandbox.domain.repository

import com.example.sandbox.domain.common.NotFoundError
import com.example.sandbox.domain.model.User
import com.github.michaelbull.result.Result
import de.huxhorn.sulky.ulid.ULID

interface UserRepository {

    fun findById(id: ULID.Value): Result<User, NotFoundError>

    fun create(user: User): Int

    fun update(updatedUser: User): Int
}
