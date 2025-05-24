package com.example.sandbox.controller

import com.example.sandbox.valueobject.Position
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @LocalServerPort
    lateinit var port: Integer

    @Nested
    inner class Create {
        @Test
        fun `Create a new user`() {
            val request = UserCreateRequest("Alice", Position.ENGINEER)
            val actual = restTemplate.postForEntity<String>("http://localhost:$port/user", request)

            actual.statusCode.shouldBe(HttpStatus.CREATED)
            actual.body.shouldBe("ok")
        }
    }

    @Nested
    inner class Get {
        @ParameterizedTest
        @CsvSource(
            value = [
                """1,'{"name":"Alice","position":"ENGINEER"}'""",
                """2,'{"name":"Bob","position":"MANAGER"}'""",
            ]
        )
        fun `Get by the existing IDs and found`(id: Int, expected: String) {
            val actual = getById(id)
            actual.statusCode.shouldBe(HttpStatus.OK)
            actual.body.shouldBe(expected)
        }

        @Test
        fun `Get by not existing ID and not found`() {
            val id = 1_000_000
            val actual = getById(id)
            actual.statusCode.shouldBe(HttpStatus.NOT_FOUND)
            actual.body.shouldBe("unknown user with id $id")
        }

        private fun getById(id: Int): ResponseEntity<String> =
            restTemplate.getForEntity<String>("http://localhost:$port/user/$id")
    }
}
