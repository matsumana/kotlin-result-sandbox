package com.example.sandbox.config.mybatis

import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class MyBatisConfig {

    @Bean
    fun sqlSessionFactory(dataSource: DataSource): SqlSessionFactory {
        val factoryBean = SqlSessionFactoryBean().apply {
            setDataSource(dataSource)
            setTypeHandlers(
                PositionTypeHandler(),
            )
        }

        return factoryBean.`object` ?: throw IllegalStateException("Failed to create SqlSessionFactory")
    }
}
