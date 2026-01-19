package com.example.sandbox.application.helper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import java.io.IOException

@SpringBootTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class TransactionHelperTest(
    private val transactionHelper: TransactionHelper,
    private val jdbcTemplate: JdbcTemplate,
) {

    @BeforeEach
    fun setUp() {
        jdbcTemplate.execute("DELETE FROM test_entity")
    }

    @Test
    fun `data should be persisted when no exception is thrown`() {
        val entity = TestEntity(id = 1, name = "Test")

        transactionHelper.binding(
            onException = { it }
        ) {
            insertEntity(entity)
        }

        val found = findEntityById(entity.id)
        found.shouldNotBeNull()
        found.name.shouldBe(entity.name)
    }

    @Test
    fun `transaction should rollback when RuntimeException is thrown`() {
        val entity = TestEntity(id = 1, name = "Test")

        transactionHelper.binding(
            onException = { it }
        ) {
            insertEntity(entity)
            throw RuntimeException("Test RuntimeException")
        }

        findEntityById(entity.id).shouldBeNull()
    }

    @Test
    fun `transaction should rollback when checked exception is thrown`() {
        val entity = TestEntity(id = 1, name = "Test")

        transactionHelper.binding(
            onException = { it }
        ) {
            insertEntity(entity)
            throw IOException("Test checked exception")
        }

        findEntityById(entity.id).shouldBeNull()
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
