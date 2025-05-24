package com.example.sandbox.repository

import com.example.sandbox.record.User
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select

@Mapper
interface UserRepository {

    @Select(
        """
        SELECT id, name, position
        FROM user
        WHERE id = #{id}
        """
    )
    fun findById(id: Int): User?
}
