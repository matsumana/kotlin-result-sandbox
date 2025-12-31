package com.example.sandbox.infrastructure.mapper

import com.example.sandbox.domain.model.User
import de.huxhorn.sulky.ulid.ULID
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
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
    fun findById(id: ULID.Value): User?

    @Insert(
        """
        INSERT INTO user (id, name, position, mail_address)
        VALUES (#{id}, #{name}, #{position}, #{mailAddress})
        """
    )
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
