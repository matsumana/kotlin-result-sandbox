package com.example.sandbox.infrastructure.config

import de.huxhorn.sulky.ulid.ULID
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

class ULIDTypeHandler : BaseTypeHandler<ULID.Value>() {
    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: ULID.Value,
        jdbcType: JdbcType?,
    ) {
        ps.setString(i, parameter.toString())
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): ULID.Value? {
        return rs.getString(columnName)?.let { ULID.parseULID(it) }
    }

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): ULID.Value? {
        return rs.getString(columnIndex)?.let { ULID.parseULID(it) }
    }

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): ULID.Value? {
        return cs.getString(columnIndex)?.let { ULID.parseULID(it) }
    }
}