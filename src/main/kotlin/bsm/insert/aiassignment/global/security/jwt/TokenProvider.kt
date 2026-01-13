package bsm.insert.aiassignment.global.security.jwt

import bsm.insert.aiassignment.domain.auth.domain.type.TokenType
import bsm.insert.aiassignment.domain.user.domain.type.Role
import bsm.insert.aiassignment.global.properties.JwtProperties
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

const val ROLE_PREFIX = "ROLE_"

@Component
class TokenProvider(
    private val jwtProperties: JwtProperties,
    private val secretKey: SecretKey
) {
    fun createAccessToken(userId: Long, role: Role): String {
        val now = Date()

        return Jwts.builder()
            .signWith(secretKey)
            .subject(userId.toString())
            .claim("role", ROLE_PREFIX + role.name)
            .claim("tokenType", TokenType.ACCESS_TOKEN)
            .issuedAt(now)
            .expiration(Date(now.time + jwtProperties.accessTokenExpirationTime))
            .compact()
    }

    fun createRefreshToken(userId: Long, role: Role): String {
        val now = Date()

        return Jwts.builder()
            .signWith(secretKey)
            .subject(userId.toString())
            .claim("role", ROLE_PREFIX + role.name)
            .claim("tokenType", TokenType.REFRESH_TOKEN)
            .issuedAt(now)
            .expiration(Date(now.time + jwtProperties.refreshTokenExpirationTime))
            .compact()
    }
}
