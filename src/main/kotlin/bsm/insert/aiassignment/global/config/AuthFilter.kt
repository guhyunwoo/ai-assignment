package bsm.insert.aiassignment.global.config

import bsm.insert.aiassignment.global.properties.JwtProperties
import bsm.insert.aiassignment.global.security.jwt.TokenResolver
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class AuthFilter(
    private val tokenResolver: TokenResolver,
    private val jwtProperties: JwtProperties,
) : OncePerRequestFilter() {

    companion object {
        private val EXCLUDE_PATHS = listOf(
            "/api/auth/signup",
            "/api/auth/login",
            "/h2-console"
        )
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return EXCLUDE_PATHS.any { pattern -> path.startsWith(pattern) }
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val headerValue = request.getHeader(jwtProperties.header)

        if (headerValue != null) {
            val token: String = tokenResolver.stripPrefixScheme(headerValue)
            val authentication: Authentication = tokenResolver.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }
}
