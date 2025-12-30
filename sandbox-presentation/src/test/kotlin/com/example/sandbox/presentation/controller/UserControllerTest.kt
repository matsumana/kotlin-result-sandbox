package com.example.sandbox.presentation.controller

import com.example.sandbox.application.dto.UserCreateRequestDto
import com.example.sandbox.application.dto.UserResponseDto
import com.example.sandbox.application.dto.UserUpdateRequestDto
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
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.reactive.server.expectBody
import java.util.stream.Stream
import javax.sql.DataSource

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
    @Autowired
    private lateinit var webClient: WebTestClient

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
            val actual = getById(
                ULID.parseULID(expected.id)
            )

            assertResponseCodeAndBody(
                actual,
                HttpStatus.OK,
                expected
            )
        }

        @Test
        fun `Get by non-existing ID and not found`() {
            val id = ULID().nextValue()
            val actual = getById(id)

            assertResponseCodeAndBody(
                actual,
                HttpStatus.NOT_FOUND,
                "unknown user with id $id"
            )
        }

        @Test
        fun `Get by invalid ULID and bad request`() {
            val id = "abc"
            val actual = webClient
                .get()
                .uri("http://localhost:$port/user/$id")
                .exchange()

            assertResponseCodeAndBody(
                actual,
                HttpStatus.BAD_REQUEST,
                "ulidString must be exactly 26 chars long."
            )
        }

        private fun argumentsProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    UserResponseDto(
                        id = idForAlice.toString(),
                        name = "Alice",
                        position = "ENGINEER",
                        mailAddress = "alice@example.com"
                    ),
                ),
                Arguments.of(
                    UserResponseDto(
                        id = idForBob.toString(),
                        name = "Bob",
                        position = "MANAGER",
                        mailAddress = "bob@example.com"
                    ),
                ),
            )
        }

        private fun getById(id: ULID.Value): ResponseSpec = webClient
            .get()
            .uri("http://localhost:$port/user/$id")
            .exchange()
    }

    @Nested
    inner class Create {
        @Test
        fun `create a new user`() {
            val name = "Foo_${ULID().nextValue()}"
            val position = "SENIOR_ENGINEER"
            val mailAddress = "foo@example.com"
            val request = UserCreateRequestDto(name, position, mailAddress)
            val actual = webClient
                .post()
                .uri("http://localhost:$port/user")
                .bodyValue(request)
                .exchange()

            actual
                .returnResult()
                .status
                .shouldBe(HttpStatus.CREATED)

            actual
                .expectBody<UserResponseDto>()
                .returnResult()
                .responseBody!!
                .shouldBeEqualToIgnoringFields(
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
            val actual = webClient
                .post()
                .uri("http://localhost:$port/user")
                .bodyValue(request)
                .exchange()

            assertResponseCodeAndBody(
                actual,
                HttpStatus.BAD_REQUEST,
                "unknown position: $position"
            )
        }

        @Test
        fun `request with invalid mail address`() {
            val position = "SENIOR_ENGINEER"
            val mailAddress = "...@example.com"
            val request = UserCreateRequestDto("Alice", position, mailAddress)
            val actual = webClient
                .post()
                .uri("http://localhost:$port/user")
                .bodyValue(request)
                .exchange()

            assertResponseCodeAndBody(
                actual,
                HttpStatus.BAD_REQUEST,
                "Invalid mail address"
            )
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
            val actual = webClient
                .post()
                .uri("http://localhost:$port/user/$id")
                .bodyValue(request)
                .exchange()

            assertResponseCodeAndBody(
                actual,
                HttpStatus.OK,
                "ok"
            )

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
            val actual = webClient
                .post()
                .uri("http://localhost:$port/user/$id")
                .bodyValue(request)
                .exchange()

            assertResponseCodeAndBody(
                actual,
                HttpStatus.NOT_FOUND,
                "unknown user with id $id"
            )
        }

        @Test
        fun `request with unknown position`() {
            val id = idForAlice
            val name = "Alice"
            val position = "FOO"
            val mailAddress = "alice@example.com"
            val request = UserUpdateRequestDto(name, position, mailAddress)
            val actual = webClient
                .post()
                .uri("http://localhost:$port/user/$id")
                .bodyValue(request)
                .exchange()

            assertResponseCodeAndBody(
                actual,
                HttpStatus.BAD_REQUEST,
                "unknown position: $position"
            )
        }

        @Test
        fun `request with invalid mail address`() {
            val id = idForAlice
            val name = "Alice"
            val position = "SENIOR_ENGINEER"
            val mailAddress = "...@example.com"
            val request = UserUpdateRequestDto(name, position, mailAddress)
            val actual = webClient
                .post()
                .uri("http://localhost:$port/user/$id")
                .bodyValue(request)
                .exchange()

            assertResponseCodeAndBody(
                actual,
                HttpStatus.BAD_REQUEST,
                "Invalid mail address"
            )
        }

        @Test
        fun `Update by invalid ULID and bad request`() {
            val id = "abc"
            val name = "baz"
            val position = "ENGINEER"
            val mailAddress = "baz@example.com"
            val request = UserUpdateRequestDto(name, position, mailAddress)
            val actual = webClient
                .post()
                .uri("http://localhost:$port/user/$id")
                .bodyValue(request)
                .exchange()

            assertResponseCodeAndBody(
                actual,
                HttpStatus.BAD_REQUEST,
                "ulidString must be exactly 26 chars long."
            )
        }
    }

    companion object {
        private val idForAlice = ULID.parseULID("01JW96H5W75VJ50D3PK85HHPXQ")
        private val idForBob = ULID.parseULID("01JW96J2PK3XC9P3PG2N35ATZ6")

        private inline fun <reified T : Any> assertResponseCodeAndBody(
            responseSpec: ResponseSpec,
            status: HttpStatus,
            body: T
        ) {
            responseSpec
                .returnResult()
                .status
                .shouldBe(status)

            responseSpec
                .expectBody<T>()
                .returnResult()
                .responseBody
                .shouldBe(body)
        }
    }
}
