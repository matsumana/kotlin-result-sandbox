package com.example.sandbox.domain.model

import com.example.sandbox.domain.valueobject.MailAddress
import com.example.sandbox.domain.valueobject.Position
import de.huxhorn.sulky.ulid.ULID

class User private constructor(
    val id: ULID.Value,
    val name: String,
    val position: Position,
    val mailAddress: MailAddress
) {

    fun copy(
        name: String = this.name,
        position: Position = this.position,
        mailAddress: MailAddress = this.mailAddress
    ): User = User(
        id = id, // keep the original id
        name = name,
        position = position,
        mailAddress = mailAddress,
    )

    companion object {
        fun create(
            name: String,
            position: Position,
            mailAddress: MailAddress
        ): User = User(
            id = generateId(),
            name = name,
            position = position,
            mailAddress = mailAddress
        )

        private fun generateId(): ULID.Value = ULID().nextValue()
    }
}
