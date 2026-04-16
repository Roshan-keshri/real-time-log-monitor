package com.roshan.logmonitor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Look for the "Authorization" header in the request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. If there is no token, or it doesn't start with "Bearer ", move along
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the token (Remove the first 7 characters: "Bearer ")
        jwt = authHeader.substring(7);

        // 4. Extract the username from the token
        username = jwtUtil.extractUsername(jwt);

        // 5. If we found a username and the user isn't already logged in
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Get the user from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 6. Check if the token is valid!
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // 7. Token is valid! Tell Spring Security to log this user in for this request
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 8. Continue to the next step
        filterChain.doFilter(request, response);
    }
}