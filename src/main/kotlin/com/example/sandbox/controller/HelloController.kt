package com.example.sandbox.controller

import com.example.sandbox.service.HelloService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController(
    private val helloService: HelloService
) {

    @GetMapping("/hello/{id}")
    fun hello(@PathVariable id: Int): String {
        val user = helloService.findById(id)
        return "Hello, ${user?.name}!"
    }
}
