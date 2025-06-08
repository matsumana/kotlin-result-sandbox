package com.example.sandbox.application.helper

import com.github.michaelbull.result.BindingScope
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition

@Component
class TransactionHelper(
    private val transactionManager: PlatformTransactionManager,
) {
    internal fun <T, E> bindingWithTransaction(
        block: BindingScope<E>.() -> T,
        errorConverter: (Exception) -> E
    ): Result<T, E> = try {
        val status = transactionManager.getTransaction(DefaultTransactionDefinition())

        try {
            val result = binding(block)
            if (result.isOk) {
                transactionManager.commit(status)
            } else {
                transactionManager.rollback(status)
            }
            result
        } catch (e: Exception) {
            transactionManager.rollback(status)
            Err(errorConverter(e))
        }
    } catch (e: Exception) {
        Err(errorConverter(e))
    }
}
