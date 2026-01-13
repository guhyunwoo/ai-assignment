package bsm.insert.aiassignment.global.security.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class AuthUserDetails(
    private val userId: String,
    private val authorities: Collection<GrantedAuthority>
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getUsername(): String = userId

    override fun getPassword(): String? = null

    fun isAdmin(): Boolean {
        return authorities.stream().anyMatch { a: GrantedAuthority -> a.authority == "ROLE_ADMIN" }
    }

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}
