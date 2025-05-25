package com.example.sandbox.repository

import com.example.sandbox.error.common.NotFoundError
import com.example.sandbox.record.User
import com.github.michaelbull.result.Result

interface UserRepository {

    fun findById(id: Int): Result<User, NotFoundError>

    fun create(user: User): Int

    fun update(updatedUser: User): Int
}
