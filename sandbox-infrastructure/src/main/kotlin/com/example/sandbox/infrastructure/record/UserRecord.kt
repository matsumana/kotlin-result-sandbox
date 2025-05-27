package com.example.sandbox.infrastructure.record

import com.example.sandbox.domain.model.User
import com.example.sandbox.domain.valueobject.MailAddress
import com.example.sandbox.domain.valueobject.Position
import de.huxhorn.sulky.ulid.ULID

data class UserRecord(
    override val id: ULID.Value,
    override val name: String,
    override val position: Position,
    override val mailAddress: MailAddress
) : User
