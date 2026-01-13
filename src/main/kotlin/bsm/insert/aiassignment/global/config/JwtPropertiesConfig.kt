package bsm.insert.aiassignment.global.config

import bsm.insert.aiassignment.global.properties.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtPropertiesConfig
