package com.example.sandbox.infrastructure.config

import com.example.sandbox.domain.valueobject.MailAddress
import com.github.michaelbull.result.getOrThrow
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

class MailAddressTypeHandler : BaseTypeHandler<MailAddress>() {
    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: MailAddress,
        jdbcType: JdbcType?,
    ) {
        ps.setString(i, parameter.value)
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): MailAddress? =
        rs.getString(columnName)?.let { ok ->
            MailAddress.create(ok)
                .getOrThrow { exception }
        }

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): MailAddress? =
        rs.getString(columnIndex)?.let { ok ->
            MailAddress.create(ok)
                .getOrThrow { exception }
        }

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): MailAddress? =
        cs.getString(columnIndex)?.let { ok ->
            MailAddress.create(ok)
                .getOrThrow { exception }
        }

    companion object {
        private val exception = IllegalArgumentException("Invalid mail address")
    }
}
