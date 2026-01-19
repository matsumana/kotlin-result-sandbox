package com.example.sandbox.application.usecase

import com.example.sandbox.application.dto.UserCreateRequestDto
import com.example.sandbox.application.dto.UserUpdateRequestDto
import com.github.michaelbull.result.getOrThrow
import com.github.michaelbull.result.onFailure
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootTest
class UserUseCaseIntegrationTest(
    private val userUseCase: UserUseCase,
    private val jdbcTemplate: JdbcTemplate,
) {

    @BeforeEach
    fun setUp() {
        jdbcTemplate.execute("DELETE FROM user")
    }

    @Nested
    inner class CreateTest {
        @Test
        fun `should persist user when all validations pass`() {
            val request = UserCreateRequestDto(
                name = "Test User",
                position = "ENGINEER",
                mailAddress = "test@example.com"
            )

            val result = userUseCase.create(request)

            val createdUser = result.getOrThrow { AssertionError("Expected success but got error: $it") }
            val found = findUserById(createdUser.id)
            found.shouldNotBeNull()
            found["name"].shouldBe("Test User")
            found["position"].shouldBe("ENGINEER")
            found["mail_address"].shouldBe("test@example.com")
        }

        @Test
        fun `should not persist when validation fails`() {
            val request = UserCreateRequestDto(
                name = "Test User",
                position = "INVALID_POSITION",
                mailAddress = "test@example.com"
            )

            userUseCase.create(request).onFailure {
                // Expected to fail
            }

            val count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user", Int::class.java)
            count.shouldBe(0)
        }
    }

    @Nested
    inner class UpdateTest {
        @Test
        fun `should persist changes when all validations pass`() {
            val createRequest = UserCreateRequestDto(
                name = "Original Name",
                position = "ENGINEER",
                mailAddress = "original@example.com"
            )
            val created = userUseCase.create(createRequest)
                .getOrThrow { AssertionError("Failed to create user: $it") }

            val updateRequest = UserUpdateRequestDto(
                name = "Updated Name",
                position = "SENIOR_ENGINEER",
                mailAddress = "updated@example.com"
            )

            userUseCase.update(created.id, updateRequest)
                .getOrThrow { AssertionError("Failed to update user: $it") }

            val found = findUserById(created.id)
            found.shouldNotBeNull()
            found["name"].shouldBe("Updated Name")
            found["position"].shouldBe("SENIOR_ENGINEER")
            found["mail_address"].shouldBe("updated@example.com")
        }

        @Test
        fun `should not change data when validation fails`() {
            val createRequest = UserCreateRequestDto(
                name = "Original Name",
                position = "ENGINEER",
                mailAddress = "original@example.com"
            )
            val created = userUseCase.create(createRequest)
                .getOrThrow { AssertionError("Failed to create user: $it") }

            val updateRequest = UserUpdateRequestDto(
                name = "Updated Name",
                position = "INVALID_POSITION",
                mailAddress = "updated@example.com"
            )

            userUseCase.update(created.id, updateRequest).onFailure {
                // Expected to fail
            }

            val found = findUserById(created.id)
            found.shouldNotBeNull()
            found["name"].shouldBe("Original Name")
            found["position"].shouldBe("ENGINEER")
            found["mail_address"].shouldBe("original@example.com")
        }
    }

    private fun findUserById(id: String): Map<String, Any>? {
        return jdbcTemplate.query(
            "SELECT id, name, position, mail_address FROM user WHERE id = ?",
            { rs, _ ->
                mapOf(
                    "id" to rs.getString("id"),
                    "name" to rs.getString("name"),
                    "position" to rs.getString("position"),
                    "mail_address" to rs.getString("mail_address")
                )
            },
            id
        ).firstOrNull()
    }
}
