package com.example.sandbox.presentation.controller

import com.example.sandbox.application.dto.UserCreateRequestDto
import com.example.sandbox.application.dto.UserResponseDto
import com.example.sandbox.application.dto.UserUpdateRequestDto
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.huxhorn.sulky.ulid.ULID
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
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
import java.util.stream.Stream
import javax.sql.DataSource

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var dataSource: DataSource

    @LocalServerPort
    private var port: Int? = null

    private lateinit var jdbcClient: JdbcClient

    @BeforeAll
    fun setup() {
        jdbcClient = JdbcClient.create(dataSource)
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Get {
        @ParameterizedTest
        @MethodSource("argumentsProvider")
        fun `Get by the existing IDs and found`(expected: UserResponseDto) {
            val actual = getById(expected.id)
            actual.statusCode.shouldBe(HttpStatus.OK)

            val actualBody: UserResponseDto = objectReader.readValue(actual.body)
            actualBody.shouldBe(expected)
        }

        @Test
        fun `Get by not existing ID and not found`() {
            val id = ULID().nextValue()
            val actual = getById(id.toString())
            actual.statusCode.shouldBe(HttpStatus.NOT_FOUND)
            actual.body.shouldBe("unknown user with id $id")
        }

        private fun argumentsProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    UserResponseDto(
                        id = ID_FOR_ALICE,
                        name = "Alice",
                        position = "ENGINEER",
                        mailAddress = "alice@example.com"
                    ),
                ),
                Arguments.of(
                    UserResponseDto(
                        id = ID_FOR_BOB,
                        name = "Bob",
                        position = "MANAGER",
                        mailAddress = "bob@example.com"
                    ),
                ),
            )
        }

        private fun getById(id: String): ResponseEntity<String> {
            val ulid = ULID.parseULID(id)
            return restTemplate.getForEntity<String>("http://localhost:$port/user/$ulid")
        }
    }

    @Nested
    inner class Create {
        @Test
        fun `create a new user`() {
            val name = "Foo_${ULID().nextValue()}"
            val position = "SENIOR_ENGINEER"
            val mailAddress = "foo@example.com"
            val request = UserCreateRequestDto(name, position, mailAddress)
            val actual = restTemplate.postForEntity<String>("http://localhost:$port/user", request)

            actual.statusCode.shouldBe(HttpStatus.CREATED)

            val actualBody: UserResponseDto = objectReader.readValue(actual.body)
            actualBody.shouldBeEqualToIgnoringFields(
                UserResponseDto(
                    id = "ignored",
                    name = name,
                    position = position,
                    mailAddress = mailAddress
                ),
                UserResponseDto::id,
            )

            val actualInserted = jdbcClient
                .sql("SELECT id, name, position, mail_address FROM user WHERE name = :name")
                .param("name", name)
                .query { rs, _ ->
                    UserResponseDto(
                        id = rs.getString("id"),
                        name = rs.getString("name"),
                        position = rs.getString("position"),
                        mailAddress = rs.getString("mail_address")
                    )
                }
                .single()

            actualInserted.shouldBeEqualToIgnoringFields(
                UserResponseDto(
                    id = "ignored",
                    name = name,
                    position = position,
                    mailAddress = mailAddress
                ),
                UserResponseDto::id,
            )
        }

        @Test
        fun `request with unknown position`() {
            val position = "FOO"
            val mailAddress = "foo@example.com"
            val request = UserCreateRequestDto("Alice", position, mailAddress)
            val actual = restTemplate.postForEntity<String>("http://localhost:$port/user", request)

            actual.statusCode.shouldBe(HttpStatus.BAD_REQUEST)
            actual.body.shouldBe("unknown position: $position")
        }

        @Test
        fun `request with invalid mail address`() {
            val position = "SENIOR_ENGINEER"
            val mailAddress = "...@example.com"
            val request = UserCreateRequestDto("Alice", position, mailAddress)
            val actual = restTemplate.postForEntity<String>("http://localhost:$port/user", request)

            actual.statusCode.shouldBe(HttpStatus.BAD_REQUEST)
            actual.body.shouldBe("Invalid mail address")
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Update {

        private val idForJohnDoe = ULID().nextValue()

        @BeforeAll
        fun setup() {
            val id = idForJohnDoe
            val name = "John Doe"
            val position = "MANAGER"
            val mailAddress = "john@example.com"
            jdbcClient.sql("INSERT INTO user (id, name, position, mail_address) VALUES (:id, :name, :position, :mailAddress)")
                .param("id", id)
                .param("name", name)
                .param("position", position)
                .param("mailAddress", mailAddress)
                .update()
        }

        @Test
        fun `Update an existing user`() {
            val id = idForJohnDoe
            val name = "Bar"
            val position = "GENERAL_MANAGER"
            val mailAddress = "bar@example.com"
            val request = UserUpdateRequestDto(name, position, mailAddress)
            val actual = restTemplate.postForEntity<String>("http://localhost:$port/user/$id", request)

            actual.statusCode.shouldBe(HttpStatus.OK)
            actual.body.shouldBe("ok")

            val actualInserted = jdbcClient
                .sql("SELECT id, name, position, mail_address FROM user WHERE id = :id")
                .param("id", id)
                .query { rs, _ ->
                    UserResponseDto(
                        id = rs.getString("id"),
                        name = rs.getString("name"),
                        position = rs.getString("position"),
                        mailAddress = rs.getString("mail_address")
                    )
                }
                .single()

            actualInserted.shouldBe(
                UserResponseDto(
                    id = id.toString(),
                    name = name,
                    position = position,
                    mailAddress = mailAddress
                )
            )
        }

        @Test
        fun `Update a non-existing user`() {
            val id = ULID().nextValue()
            val name = "baz"
            val position = "ENGINEER"
            val mailAddress = "baz@example.com"
            val request = UserUpdateRequestDto(name, position, mailAddress)
            val actual = restTemplate.postForEntity<String>("http://localhost:$port/user/$id", request)

            actual.statusCode.shouldBe(HttpStatus.NOT_FOUND)
            actual.body.shouldBe("unknown user with id $id")
        }

        @Test
        fun `request with unknown position`() {
            val id = ULID.parseULID(ID_FOR_ALICE)
            val name = "Alice"
            val position = "FOO"
            val mailAddress = "alice@example.com"
            val request = UserUpdateRequestDto(name, position, mailAddress)
            val actual = restTemplate.postForEntity<String>("http://localhost:$port/user/$id", request)

            actual.statusCode.shouldBe(HttpStatus.BAD_REQUEST)
            actual.body.shouldBe("unknown position: $position")
        }

        @Test
        fun `request with invalid mail address`() {
            val id = ULID.parseULID(ID_FOR_ALICE)
            val name = "Alice"
            val position = "SENIOR_ENGINEER"
            val mailAddress = "...@example.com"
            val request = UserUpdateRequestDto(name, position, mailAddress)
            val actual = restTemplate.postForEntity<String>("http://localhost:$port/user/$id", request)

            actual.statusCode.shouldBe(HttpStatus.BAD_REQUEST)
            actual.body.shouldBe("Invalid mail address")
        }
    }

    companion object {
        private const val ID_FOR_ALICE = "01JW96H5W75VJ50D3PK85HHPXQ"

        private const val ID_FOR_BOB = "01JW96J2PK3XC9P3PG2N35ATZ6"

        private val typeRef = object : TypeReference<UserResponseDto>() {}

        private val objectReader = ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .readerFor(typeRef)
    }
}
