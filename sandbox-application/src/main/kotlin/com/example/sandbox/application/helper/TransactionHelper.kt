package com.example.sandbox.application.helper

import com.github.michaelbull.result.BindingScope
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class TransactionHelper(
    private val transactionTemplate: TransactionTemplate,
) {
    fun <V, E> binding(
        onException: (Exception) -> E,
        block: BindingScope<E>.() -> V
    ): Result<V, E> = try {
        transactionTemplate.execute { status ->
            com.github.michaelbull.result.binding(block)
                .onFailure { status.setRollbackOnly() }
        }
    } catch (e: Exception) {
        Err(onException(e))
    }
}
