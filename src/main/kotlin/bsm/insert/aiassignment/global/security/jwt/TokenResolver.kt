package bsm.insert.aiassignment.global.security.jwt

import bsm.insert.aiassignment.domain.auth.domain.type.TokenType
import bsm.insert.aiassignment.global.properties.JwtProperties
import bsm.insert.aiassignment.global.security.auth.AuthUserDetails
import bsm.insert.aiassignment.global.security.jwt.exception.ExpiredTokenException
import bsm.insert.aiassignment.global.security.jwt.exception.InvalidTokenException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import javax.crypto.SecretKey


@Component
class TokenResolver(
    private val jwtProperties: JwtProperties,
    private val secretKey: SecretKey,
    private val tokenValidator: TokenValidator,
) {
    fun stripPrefixScheme(authorizationHeader: String): String {
        tokenValidator.validateAuthorizationHeader(authorizationHeader)
        return authorizationHeader.substring(jwtProperties.prefix.length).trim()
    }

    fun getAuthentication(token: String): Authentication {
        val claims = getTokenBody(token)

        val userId = claims.subject

        @Suppress("UNCHECKED_CAST")
        val roles = claims["userRole"] as List<String>

        val authorities: List<GrantedAuthority> =
            roles.map { SimpleGrantedAuthority(it) }

        val principal = AuthUserDetails(userId, authorities)

        return UsernamePasswordAuthenticationToken(
            principal,
            null,
            authorities
        )
    }

    private fun getTokenBody(token: String): Claims {
        try {
            val tokenBody = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload

            val extractedTokenType = extractTokenType(tokenBody)
            tokenValidator.validateTokenType(extractedTokenType, TokenType.ACCESS_TOKEN)

            return tokenBody
        } catch (e: ExpiredJwtException) {
            throw ExpiredTokenException()
        } catch (e: JwtException) {
            throw InvalidTokenException()
        }
    }

    private fun extractTokenType(tokenBody: Claims): TokenType {
        val tokenTypeString = tokenBody["tokenType"]?.toString()
            ?: throw InvalidTokenException()
        return try {
            TokenType.valueOf(tokenTypeString)
        } catch (e: IllegalArgumentException) {
            throw InvalidTokenException()
        }
    }
}
