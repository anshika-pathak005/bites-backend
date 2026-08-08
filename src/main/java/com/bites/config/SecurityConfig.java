package com.bites.config;

import com.bites.security.GuestIdentityFilter;
import com.bites.security.JwtAccessDeniedHandler;
import com.bites.security.JwtAuthenticationEntryPoint;
import com.bites.security.JwtAuthenticationFilter;
import com.bites.security.LoginRateLimitFilter;
import com.bites.security.OrderRateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
 * Configures Spring Security for the whole app: which endpoints are public
 * (like browsing the menu or checking out as a guest) and which require a
 * logged-in admin, plus how passwords are hashed and how JWT tokens are
 * checked on incoming requests.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Filter that checks incoming requests for a valid JWT and logs the admin in for that request.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Filter that throttles repeated attempts against the admin login endpoint.
    private final LoginRateLimitFilter loginRateLimitFilter;

    // Filter that throttles order tracking/cancellation/checkout — the
    // endpoints that matter most now that order NUMBER alone is the bearer
    // credential for tracking/cancelling (see OrderRateLimitFilter).
    private final OrderRateLimitFilter orderRateLimitFilter;

    // Filter that resolves/issues the HttpOnly guestId cookie on every
    // request — the identity everything (cart/addresses/orders) hangs off,
    // with no login involved.
    private final GuestIdentityFilter guestIdentityFilter;

    // Returns 401 for missing/invalid/expired tokens, instead of Spring Security's default 403.
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // Returns 403 for a valid token that lacks the required role.
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // Injects the JWT/rate-limit/guest-identity filters and the 401/403 handlers so they can be wired into the security filter chain below.
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, LoginRateLimitFilter loginRateLimitFilter,
                           OrderRateLimitFilter orderRateLimitFilter, GuestIdentityFilter guestIdentityFilter,
                           JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.loginRateLimitFilter = loginRateLimitFilter;
        this.orderRateLimitFilter = orderRateLimitFilter;
        this.guestIdentityFilter = guestIdentityFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    /*
     * Provides the password hashing algorithm (BCrypt) used whenever an
     * admin password is created or checked.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * Defines the main security rules for every incoming HTTP request:
     * disables CSRF (not needed for a stateless JWT API), enables CORS,
     * keeps the app stateless (no server-side sessions), sets some
     * protective HTTP headers, marks which endpoints are public vs. require
     * login, and plugs in the JWT filter to authenticate requests.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // uses CorsConfigurationSource bean from CorsConfig
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .contentTypeOptions(contentTypeOptions -> {})
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true))
                )
                .authorizeHttpRequests(auth -> auth
                        // Public: browsing, guest cart, checkout, order tracking, admin login, docs
                        .requestMatchers(
                                "/api/categories/**",
                                "/api/menu-items/**",
                                "/api/delivery-areas/**",
                                "/api/cart/**",
                                "/api/addresses/**",
                                "/api/delivery-pricing/**", // GET quote — public, read-only preview of the delivery charge
                                "/api/orders/track/**",
                                "/api/orders", // POST checkout, GET list-mine — guest, cookie-scoped inside the service
                                "/api/orders/*/cancel", // POST cancel — guest, order-number is the credential
                                "/api/restaurant",
                                "/api/admin/auth/**",
                                "/docs/**", "/api-docs/**", "/swagger-ui/**"
                        ).permitAll()
                        // Everything else under /api/admin/** requires a valid admin JWT
                        .requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(orderRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(guestIdentityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
