package com.example.sandbox.service

import com.example.sandbox.record.User
import com.example.sandbox.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class HelloService(
    private val employeeRepository: UserRepository
) {
    fun findById(id: Int): User? = employeeRepository.findById(id)
}
