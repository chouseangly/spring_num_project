package com.example.spring_project_mid.config;

import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserStatusFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Check if there is a logged-in user (who is not anonymous)
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {

            String username = auth.getName();
            Optional<User> dbUser = userRepository.findByUsername(username);

            // If user exists in DB but is NOT enabled (Suspended)
            if (dbUser.isPresent() && !dbUser.get().isEnabled()) {
                // 1. Clear the security context (Log them out)
                SecurityContextHolder.clearContext();

                // 2. Invalidate the session (Destroy the cookie)
                if (request.getSession(false) != null) {
                    request.getSession().invalidate();
                }

                // 3. Redirect to login page with an error
                response.sendRedirect("/login?error");
                return; // Stop the request here
            }
        }

        filterChain.doFilter(request, response);
    }
}