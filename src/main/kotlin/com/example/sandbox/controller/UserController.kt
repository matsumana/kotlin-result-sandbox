package com.example.sandbox.controller

import com.example.sandbox.controller.dto.UserCreateRequest
import com.example.sandbox.controller.dto.UserUpdateRequest
import com.example.sandbox.exception.api.BadRequestException
import com.example.sandbox.exception.api.NotFoundException
import com.example.sandbox.record.User
import com.example.sandbox.usecase.UserUseCase
import com.example.sandbox.usecase.UserUseCase.CreateResult
import com.example.sandbox.usecase.UserUseCase.FindByIdResult
import com.github.michaelbull.result.mapBoth
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(
    private val userUseCase: UserUseCase
) {

    @GetMapping("/{id}")
    fun get(@PathVariable id: Int): ResponseEntity<User> =
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
    fun create(@RequestBody request: UserCreateRequest): ResponseEntity<User> =
        userUseCase.create(request)
            .mapBoth(
                success = { ResponseEntity(it, HttpStatus.CREATED) },
                failure = { err ->
                    when (err) {
                        is CreateResult.EnumConvertError -> throw BadRequestException(err.message)
                    }
                }
            )

    @PostMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: UserUpdateRequest): ResponseEntity<String> =
        userUseCase.update(
            id,
            request
        ).mapBoth(
            success = { ResponseEntity("ok", HttpStatus.OK) },
            failure = { err ->
                when (err) {
                    is UserUseCase.UpdateResult.NotFoundError -> throw NotFoundException(err.message)

                    is UserUseCase.UpdateResult.EnumConvertError -> throw BadRequestException(err.message)
                }
            }
        )
}
