package bsm.insert.aiassignment.global.config

import bsm.insert.aiassignment.domain.common.snowflake.Snowflake
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SnowflakeConfig(
    @param:Value("\${snowflake.node-id}")
    private val nodeId: Long,
) {
    @Bean
    fun snowflake(): Snowflake = Snowflake(nodeId)
}