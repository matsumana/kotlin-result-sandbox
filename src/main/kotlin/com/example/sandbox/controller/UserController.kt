package com.example.sandbox.controller

import com.example.sandbox.usecase.UserUseCase
import com.example.sandbox.usecase.UserUseCase.FindByIdResult
import com.github.michaelbull.result.mapBoth
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    private val userUseCase: UserUseCase
) {

    @GetMapping("/{id}")
    fun get(@PathVariable id: Int): ResponseEntity<String> =
        userUseCase.findById(id)
            .mapBoth(
                success = { ok ->
                    ResponseEntity(ok.name, HttpStatus.OK)
                },
                failure = { err ->
                    when (err) {
                        is FindByIdResult.NotFound ->
                            ResponseEntity(err.message, HttpStatus.NOT_FOUND)
                    }
                }
            )
}
