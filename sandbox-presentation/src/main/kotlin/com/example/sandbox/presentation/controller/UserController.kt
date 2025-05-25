package com.example.sandbox.presentation.controller

import com.example.sandbox.application.dto.UserCreateRequestDto
import com.example.sandbox.application.dto.UserResponseDto
import com.example.sandbox.application.dto.UserUpdateRequestDto
import com.example.sandbox.application.usecase.UserUseCase
import com.example.sandbox.application.usecase.UserUseCase.CreateResult
import com.example.sandbox.application.usecase.UserUseCase.FindByIdResult
import com.example.sandbox.presentation.exception.BadRequestException
import com.example.sandbox.presentation.exception.NotFoundException
import com.github.michaelbull.result.mapBoth
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
    fun get(@PathVariable id: Int): ResponseEntity<UserResponseDto> =
        userUseCase.findById(id)
            .mapBoth(
                success = { ResponseEntity(it, HttpStatus.OK) },
                failure = { err ->
                    when (err) {
                        is FindByIdResult.NotFoundError -> throw NotFoundException(err.message)
                    }
                }
            )

    @PostMapping
    fun create(@RequestBody request: UserCreateRequestDto): ResponseEntity<UserResponseDto> =
        userUseCase.create(request)
            .mapBoth(
                success = { ResponseEntity(it, HttpStatus.CREATED) },
                failure = { err ->
                    when (err) {
                        is CreateResult.EnumConvertError -> throw BadRequestException(err.message)
                        is CreateResult.InvalidMailAddressError -> throw BadRequestException("Invalid mail address")
                    }
                }
            )

    @PostMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: UserUpdateRequestDto): ResponseEntity<String> =
        userUseCase.update(
            id,
            request
        ).mapBoth(
            success = { ResponseEntity("ok", HttpStatus.OK) },
            failure = { err ->
                when (err) {
                    is UserUseCase.UpdateResult.NotFoundError -> throw NotFoundException(err.message)
                    is UserUseCase.UpdateResult.EnumConvertError -> throw BadRequestException(err.message)
                    is UserUseCase.UpdateResult.InvalidMailAddressError -> throw BadRequestException("Invalid mail address")
                }
            }
        )
}
