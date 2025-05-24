package com.example.sandbox.controller

import com.example.sandbox.controller.dto.UserCreateRequest
import com.example.sandbox.controller.dto.UserGetResponse
import com.example.sandbox.controller.dto.UserUpdateRequest
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
    fun get(@PathVariable id: Int): ResponseEntity<Any> =
        userUseCase.findById(id)
            .mapBoth(
                success = { ok ->
                    ResponseEntity(
                        UserGetResponse(
                            name = ok.name,
                            position = ok.position
                        ),
                        HttpStatus.OK
                    )
                },
                failure = { err ->
                    when (err) {
                        is FindByIdResult.NotFoundError ->
                            ResponseEntity(err.message, HttpStatus.NOT_FOUND)
                    }
                }
            )

    @PostMapping
    fun create(@RequestBody request: UserCreateRequest): ResponseEntity<Any> =
        userUseCase.create(request)
            .mapBoth(
                success = { ok ->
                    val generatedId = ok.id

                    ResponseEntity(generatedId, HttpStatus.CREATED)
                },
                failure = { err ->
                    when (err) {
                        is CreateResult.EnumConvertError ->
                            ResponseEntity(err.message, HttpStatus.BAD_REQUEST)
                    }
                }
            )

    @PostMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: UserUpdateRequest): ResponseEntity<String> =
        userUseCase.update(
            User(
                id = id,
                name = request.name,
                position = request.position
            )
        ).mapBoth(
            success = { updatedId ->
                ResponseEntity("ok", HttpStatus.OK)
            },
            failure = { err ->
                when (err) {
                    is UserUseCase.UpdateResult.NotFoundError ->
                        ResponseEntity(err.message, HttpStatus.NOT_FOUND)
                }
            }
        )
}
