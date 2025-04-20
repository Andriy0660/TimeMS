package com.example.timecraft.domain.auth.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.domain.user.api.UserService;
import com.example.timecraft.domain.user.persistence.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccessTokenFilter extends OncePerRequestFilter {
    private final UserService userService;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        final String accessToken = request.getHeader("Authorization");
        if (accessToken == null) {
            if (!request.getRequestURI().matches(".*auth.*")) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            } else {
                filterChain.doFilter(request, response);
            }
            return;
        }

        final UserEntity user;
        try {
            user = userService.findByAccessToken(accessToken);

            if (!user.isActive()) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.getWriter().write("Account is disabled");
                return;
            }

            Set<SimpleGrantedAuthority> authorities = new HashSet<>();
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toSet());
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (NotFoundException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        filterChain.doFilter(request, response);
    }
}