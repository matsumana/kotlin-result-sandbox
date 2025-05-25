package com.example.sandbox.controller

import com.example.sandbox.domain.model.User
import com.example.sandbox.domain.valueobject.Position
import com.example.sandbox.dto.UserCreateRequestDto
import com.example.sandbox.dto.UserUpdateRequestDto
import com.github.michaelbull.result.get
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
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
import org.springframework.jdbc.core.simple.JdbcClient
import javax.sql.DataSource

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var dataSource: DataSource

    @LocalServerPort
    lateinit var port: Integer

    private lateinit var jdbcClient: JdbcClient

    @BeforeAll
    fun setup() {
        jdbcClient = JdbcClient.create(dataSource)
    }

    @Nested
    inner class Get {
        @ParameterizedTest
        @CsvSource(
            value = [
                """ 1, '{"id":1,"name":"Alice","position":"ENGINEER"}' """,
                """ 2, '{"id":2,"name":"Bob","position":"MANAGER"}' """,
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

    @Nested
    inner class Create {
        @Test
        fun `create a new user`() {
            val id = 3 // Assuming the next ID is 3
            val name = "Foo"
            val position = Position.SENIOR_ENGINEER.toString()
            val request = UserCreateRequestDto(name, position)
            val actual = restTemplate.postForEntity<String>("http://localhost:$port/user", request)

            actual.statusCode.shouldBe(HttpStatus.CREATED)
            actual.body.shouldBe("""{"id":$id,"name":"$name","position":"$position"}""")

            val actualInserted = jdbcClient
                .sql("SELECT id, name, position FROM user WHERE id = :id")
                .param("id", id)
                .query { rs, _ ->
                    User.create(
                        id = rs.getInt("id"),
                        name = rs.getString("name"),
                        position = rs.getString("position")
                    ).get()
                }
                .single()

            actualInserted.shouldBe(
                User.create(
                    id = id,
                    name = name,
                    position = position
                ).get()
            )
        }

        @Test
        fun `request with unknown position`() {
            val position = "FOO"
            val request = UserCreateRequestDto("Alice", position)
            val actual = restTemplate.postForEntity<String>("http://localhost:$port/user", request)

            actual.statusCode.shouldBe(HttpStatus.BAD_REQUEST)
            actual.body.shouldBe("unknown position: $position")
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Update {

        @BeforeAll
        fun setup() {
            val id = 100
            val name = "John Doe"
            val position = Position.MANAGER.toString()
            jdbcClient.sql("INSERT INTO user (id, name, position) VALUES (:id, :name, :position)")
                .param("id", id)
                .param("name", name)
                .param("position", position)
                .update()
        }

        @Test
        fun `Update an existing user`() {
            val id = 100
            val name = "Bar"
            val position = Position.GENERAL_MANAGER.toString()
            val request = UserUpdateRequestDto(name, position)
            val actual = restTemplate.postForEntity<String>("http://localhost:$port/user/$id", request)

            actual.statusCode.shouldBe(HttpStatus.OK)
            actual.body.shouldBe("ok")

            val actualInserted = jdbcClient
                .sql("SELECT id, name, position FROM user WHERE id = :id")
                .param("id", id)
                .query { rs, _ ->
                    User.create(
                        id = rs.getInt("id"),
                        name = rs.getString("name"),
                        position = rs.getString("position")
                    ).get()
                }
                .single()

            actualInserted.shouldBe(
                User.create(
                    id = id,
                    name = name,
                    position = position
                ).get()
            )
        }

        @Test
        fun `Update a non-existing user`() {
            val id = 1_000_000
            val request = UserUpdateRequestDto("Alice", Position.ENGINEER.toString())
            val actual = restTemplate.postForEntity<String>("http://localhost:$port/user/$id", request)

            actual.statusCode.shouldBe(HttpStatus.NOT_FOUND)
            actual.body.shouldBe("unknown user with id $id")
        }

        @Test
        fun `request with unknown position`() {
            val id = 1
            val position = "FOO"
            val request = UserUpdateRequestDto("Alice", position)
            val actual = restTemplate.postForEntity<String>("http://localhost:$port/user/$id", request)

            actual.statusCode.shouldBe(HttpStatus.BAD_REQUEST)
            actual.body.shouldBe("unknown position: $position")
        }
    }
}
