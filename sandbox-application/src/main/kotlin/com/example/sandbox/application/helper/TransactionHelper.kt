package com.example.sandbox.application.helper

import com.github.michaelbull.result.BindingScope
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.lang.reflect.UndeclaredThrowableException

@Component
class TransactionHelper(
    private val transactionTemplate: TransactionTemplate,
) {
    fun <V, E> binding(
        onException: (Throwable) -> E,
        block: BindingScope<E>.() -> Result<V, E>
    ): Result<V, E> = try {
        transactionTemplate.execute { status ->
            com.github.michaelbull.result.binding<V, E> {
                block().bind()
            }.onFailure {
                status.setRollbackOnly()
            }
        }
    } catch (e: UndeclaredThrowableException) {
        // TransactionTemplate#execute throws UndeclaredThrowableException when the block throws a checked exception.
        // So unwrap the cause and pass it to onException
        val cause = e.cause
        if (cause != null) {
            Err(onException(cause))
        } else {
            Err(onException(e))
        }
    } catch (e: Exception) {
        Err(onException(e))
    }
}
