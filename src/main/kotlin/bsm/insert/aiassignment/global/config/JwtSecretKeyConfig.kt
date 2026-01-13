package bsm.insert.aiassignment.global.config

import bsm.insert.aiassignment.global.properties.JwtProperties
import io.jsonwebtoken.security.Keys
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Base64
import javax.crypto.SecretKey

@Configuration
class JwtSecretKeyConfig(
    private val jwtProperties: JwtProperties
) {
    @get:Bean
    val decodedKey: SecretKey
        get() = Keys.hmacShaKeyFor(
            Base64.getDecoder().decode(jwtProperties.secretKey)
        )
}