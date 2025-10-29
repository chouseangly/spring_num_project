package com.example.spring_project_mid.config;

import com.example.spring_project_mid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// Import SessionCreationPolicy if not already imported
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // For @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    // Inject JwtAuthenticationFilter directly if not already done via constructor
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Keep CSRF disabled for stateless API + simple form login
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/verify-otp",
                                "/forgot-password", // Add if you create this page
                                "/reset-password",  // Add if you create this page
                                "/api/auth/**",
                                "/css/**",       // Allow static resources if needed
                                "/js/**",        // Allow static resources if needed
                                "/images/**"     // Allow static resources if needed
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // --- START: Form Login Configuration ---
                .formLogin(form -> form
                        .loginPage("/login") // Specify custom login page URL
                        .loginProcessingUrl("/login") // URL where Spring Security handles POST
                        .defaultSuccessUrl("/home", true) // Redirect to /home on successful login
                        .failureUrl("/login?error") // Redirect back to login page with error param
                        .permitAll() // Allow access to the login page itself
                )
                // --- END: Form Login Configuration ---
                // --- START: Logout Configuration (Optional but Recommended) ---
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL to trigger logout
                        .logoutSuccessUrl("/login?logout") // Redirect after logout
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID") // Optional: clear cookies
                        .permitAll()
                )
                // --- END: Logout Configuration ---

                // --- Session Management: Important for combining form login and stateless API ---
                // If primarily using JWT/API, keep STATELESS.
                // If primarily using form login with sessions, use IF_REQUIRED or ALWAYS.
                // For now, let's keep STATELESS as your initial setup used JWT heavily.
                // Be aware this means form login won't create a persistent session by default.
                // If you need sessions for web users, change this.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authenticationProvider(authenticationProvider())
                // Add JWT filter *before* the standard form login filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Load user by username OR email
        return username -> userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}