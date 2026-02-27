package com.example.sandbox.application.helper

import com.example.sandbox.application.helper.TransactionHelperTest.TestError.DomainError
import com.example.sandbox.application.helper.TransactionHelperTest.TestError.SystemError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getErrorOrElse
import io.kotest.assertions.fail
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import java.io.IOException

@SpringBootTest
class TransactionHelperTest(
    private val transactionHelper: TransactionHelper,
    private val jdbcTemplate: JdbcTemplate,
) {

    sealed interface TestError {
        data class DomainError(val message: String) : TestError
        data class SystemError(val e: Throwable) : TestError
    }

    @BeforeEach
    fun setUp() {
        jdbcTemplate.execute("DELETE FROM test_entity")
    }

    @Nested
    inner class SuccessTest {
        @Test
        fun `data should be persisted when no exception is thrown`() {
            val entity = TestEntity(id = 1, name = "Test")

            transactionHelper.binding(
                onException = { it }
            ) {
                Ok(
                    insertEntity(entity)
                )
            }

            val found = findEntityById(entity.id)
            found.shouldNotBeNull()
            found.name.shouldBe(entity.name)
        }
    }

    @Nested
    inner class RuntimeExceptionTest {
        @Test
        fun `transaction should rollback when RuntimeException is thrown`() {
            val entity = TestEntity(id = 1, name = "Test")
            val exception = RuntimeException("Test RuntimeException")

            val result = transactionHelper.binding<Unit, SystemError>(
                onException = { SystemError(it) }
            ) {
                insertEntity(entity)
                throw exception
            }

            result.getErrorOrFail().shouldBe(SystemError(exception))

            findEntityById(entity.id).shouldBeNull()
        }
    }

    @Nested
    inner class CheckedExceptionTest {
        @Test
        fun `transaction should rollback when checked exception is thrown`() {
            val entity = TestEntity(id = 1, name = "Test")
            val exception = IOException("Test checked exception")

            val result = transactionHelper.binding<Unit, SystemError>(
                onException = { e -> SystemError(e) }
            ) {
                insertEntity(entity)
                throw exception
            }

            result.getErrorOrFail().shouldBe(SystemError(exception))
            findEntityById(entity.id).shouldBeNull()
        }
    }

    @Nested
    inner class ErrTest {
        @Test
        fun `transaction should rollback when Err is returned`() {
            val entity = TestEntity(id = 1, name = "Test")
            val exception = RuntimeException("Test error")

            transactionHelper.binding<Unit, TestError>(
                onException = { e -> SystemError(e) }
            ) {
                insertEntity(entity)
                Err(DomainError("error"))
            }

            findEntityById(entity.id).shouldBeNull()
        }
    }

    private fun insertEntity(entity: TestEntity) {
        jdbcTemplate.update(
            "INSERT INTO test_entity (id, name) VALUES (?, ?)",
            entity.id,
            entity.name
        )
    }

    private fun findEntityById(id: Int): TestEntity? {
        return jdbcTemplate.query(
            "SELECT id, name FROM test_entity WHERE id = ?",
            { rs, _ -> TestEntity(rs.getInt("id"), rs.getString("name")) },
            id
        ).firstOrNull()
    }

    private data class TestEntity(
        val id: Int,
        val name: String,
    )
}

fun <V, E> Result<V, E>.getErrorOrFail(): E {
    return this.getErrorOrElse {
        fail("expected error but got success")
    }
}
