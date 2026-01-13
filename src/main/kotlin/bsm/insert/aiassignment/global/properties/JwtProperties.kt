package bsm.insert.aiassignment.global.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("jwt")
data class JwtProperties(
    val accessTokenExpirationTime: Long,
    val refreshTokenExpirationTime: Long,
    val prefix: String,
    val header: String,
    val secretKey: String
)
