package com.example.sandbox.config.mybatis

import com.example.sandbox.valueobject.Position
import com.github.michaelbull.result.getOrThrow
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

class PositionTypeHandler : BaseTypeHandler<Position>() {
    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: Position,
        jdbcType: JdbcType?,
    ) {
        ps.setString(i, parameter.toString())
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): Position? =
        rs.getString(columnName)?.let { ok ->
            Position.of(ok)
                .getOrThrow { toException(it) }
        }

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): Position? =
        rs.getString(columnIndex)?.let { ok ->
            Position.of(ok)
                .getOrThrow { toException(it) }
        }

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): Position? =
        cs.getString(columnIndex)?.let { ok ->
            Position.of(ok)
                .getOrThrow { toException(it) }
        }

    private fun toException(err: Position.ConvertError) = IllegalArgumentException(err.message)
}
