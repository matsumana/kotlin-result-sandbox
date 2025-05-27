package com.example.sandbox.presentation.controller

import com.example.sandbox.application.dto.UserCreateRequestDto
import com.example.sandbox.application.dto.UserResponseDto
import com.example.sandbox.application.dto.UserUpdateRequestDto
import com.example.sandbox.application.usecase.UserUseCase
import com.example.sandbox.application.usecase.UserUseCase.CreateResult
import com.example.sandbox.application.usecase.UserUseCase.FindByIdResult
import com.example.sandbox.application.usecase.UserUseCase.UpdateResult
import com.example.sandbox.presentation.exception.BadRequestException
import com.example.sandbox.presentation.exception.NotFoundException
import com.github.michaelbull.result.getOrThrow
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    private val userUseCase: UserUseCase
) {

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ResponseEntity<UserResponseDto> =
        userUseCase.findById(
            id
        ).getOrThrow { err ->
            when (err) {
                is FindByIdResult.NotFoundError -> NotFoundException(err.message)
            }
        }.let {
            ResponseEntity(it, HttpStatus.OK)
        }

    @PostMapping
    fun create(@RequestBody request: UserCreateRequestDto): ResponseEntity<UserResponseDto> =
        userUseCase.create(
            request
        ).getOrThrow { err ->
            when (err) {
                is CreateResult.EnumConvertError -> BadRequestException(err.message)
                is CreateResult.InvalidMailAddressError -> BadRequestException("Invalid mail address")
            }
        }.let {
            ResponseEntity(it, HttpStatus.CREATED)
        }

    @PostMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody request: UserUpdateRequestDto): ResponseEntity<String> =
        userUseCase.update(
            id,
            request
        ).getOrThrow { err ->
            when (err) {
                is UpdateResult.NotFoundError -> NotFoundException(err.message)
                is UpdateResult.EnumConvertError -> BadRequestException(err.message)
                is UpdateResult.InvalidMailAddressError -> BadRequestException("Invalid mail address")
            }
        }.let {
            ResponseEntity("ok", HttpStatus.OK)
        }
}
