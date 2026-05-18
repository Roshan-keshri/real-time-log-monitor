package com.roshan.logmonitor.security;

import com.roshan.logmonitor.entity.Company;
import com.roshan.logmonitor.repository.CompanyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private final CompanyRepository companyRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Company> companyOptional = companyRepository.findByApiKey(apiKey);

        if (companyOptional.isPresent()) {
            Company company = companyOptional.get();

            // Put a special nametag on this request so the Service knows it's a machine!
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    "API_KEY_" + company.getId(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_MACHINE"))
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid API Key");
            return;
        }

        filterChain.doFilter(request, response);
    }
}