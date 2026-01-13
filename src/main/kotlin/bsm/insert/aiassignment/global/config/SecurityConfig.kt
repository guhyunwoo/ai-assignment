package bsm.insert.aiassignment.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val authFilter: AuthFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }

        http.sessionManagement { session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }

        http.addFilterBefore(
            authFilter,
            UsernamePasswordAuthenticationFilter::class.java
        )

        http.authorizeHttpRequests { authorize ->
            authorize
                .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
        }

        http.headers { it.frameOptions { frame -> frame.disable() } }

        return http.build()
    }
}
