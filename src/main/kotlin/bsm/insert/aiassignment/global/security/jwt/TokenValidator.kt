package bsm.insert.aiassignment.global.security.jwt

import bsm.insert.aiassignment.domain.auth.domain.type.TokenType
import bsm.insert.aiassignment.global.properties.JwtProperties
import bsm.insert.aiassignment.global.security.jwt.exception.InvalidTokenException
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

@Component
class TokenValidator(
    private val jwtProperties: JwtProperties,
) {
    fun validateAuthorizationHeader(authorizationHeader: String) {
        if (!StringUtils.hasText(authorizationHeader)) {
            throw InvalidTokenException()
        }

        if (!authorizationHeader.startsWith(jwtProperties.prefix)) {
            throw InvalidTokenException()
        }
    }

    fun validateTokenType(tokenType: TokenType, expectedType: TokenType) {
        if (tokenType !== expectedType) {
            throw InvalidTokenException()
        }
    }
}
