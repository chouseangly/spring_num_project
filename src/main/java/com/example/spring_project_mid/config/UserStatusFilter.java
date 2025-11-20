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

    /**
     * Filters incoming HTTP requests to check if the authenticated user's account is enabled.
     * If the account is disabled, it clears the security context, invalidates the session,
     * and redirects the user to the login page with an error message.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {

            String username = auth.getName();
            Optional<User> dbUser = userRepository.findByUsername(username);

            if (dbUser.isPresent() && !dbUser.get().isEnabled()) {
                SecurityContextHolder.clearContext();

                if (request.getSession(false) != null) {
                    request.getSession().invalidate();
                }

                response.sendRedirect("/login?error");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}