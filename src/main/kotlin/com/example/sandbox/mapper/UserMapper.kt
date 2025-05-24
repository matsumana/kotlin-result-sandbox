package com.example.sandbox.mapper

import com.example.sandbox.record.User
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update

@Mapper
interface UserMapper {

    @Select(
        """
        SELECT id, name, position
        FROM user
        WHERE id = #{id}
        """
    )
    fun findById(id: Int): User?

    @Insert(
        """
        INSERT INTO user (name, position)
        VALUES (#{name}, #{position})
        """
    )
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun create(user: User): Int

    @Update(
        """
        UPDATE user
        SET name = #{name}, position = #{position}
        WHERE id = #{id}
        """
    )
    fun update(updatedUser: User): Int
}