package com.example.sandbox.presentation.controller

import com.example.sandbox.application.dto.UserCreateRequestDto
import com.example.sandbox.application.dto.UserResponseDto
import com.example.sandbox.application.dto.UserUpdateRequestDto
import com.example.sandbox.application.usecase.UserUseCase
import com.example.sandbox.application.usecase.UserUseCase.CreateError
import com.example.sandbox.application.usecase.UserUseCase.FindByIdError
import com.example.sandbox.application.usecase.UserUseCase.UpdateError
import com.example.sandbox.presentation.exception.BadRequestException
import com.example.sandbox.presentation.exception.NotFoundException
import com.github.michaelbull.result.getOrThrow
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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
                is FindByIdError.InvalidULIDError -> BadRequestException(err.message)
                is FindByIdError.NotFoundError -> NotFoundException(err.message)
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
                is CreateError.EnumConvertError -> BadRequestException(err.message)
                is CreateError.InvalidMailAddressError -> BadRequestException("Invalid mail address")
                is CreateError.ExceptionOccurredError -> err.exception
            }
        }.let {
            ResponseEntity(it, HttpStatus.CREATED)
        }

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody request: UserUpdateRequestDto): ResponseEntity<String> =
        userUseCase.update(
            id,
            request
        ).getOrThrow { err ->
            when (err) {
                is UpdateError.InvalidULIDError -> BadRequestException(err.message)
                is UpdateError.NotFoundError -> NotFoundException(err.message)
                is UpdateError.EnumConvertError -> BadRequestException(err.message)
                is UpdateError.InvalidMailAddressError -> BadRequestException("Invalid mail address")
                is UpdateError.ExceptionOccurredError -> err.exception
            }
        }.let {
            ResponseEntity("ok", HttpStatus.OK)
        }
}
