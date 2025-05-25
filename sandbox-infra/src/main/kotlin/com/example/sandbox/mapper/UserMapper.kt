package com.example.sandbox.mapper

import com.example.sandbox.domain.model.User
import com.example.sandbox.record.UserRecord
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update

@Mapper
interface UserMapper {

    @Select(
        """
        SELECT id, name, position, mail_address
        FROM user
        WHERE id = #{id}
        """
    )
    fun findById(id: Int): UserRecord?

    @Insert(
        """
        INSERT INTO user (name, position, mail_address)
        VALUES (#{name}, #{position}, #{mailAddress})
        """
    )
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun create(user: User): Int

    @Update(
        """
        UPDATE user
        SET name = #{name}, position = #{position}, mail_address = #{mailAddress}
        WHERE id = #{id}
        """
    )
    fun update(updatedUser: User): Int
}
