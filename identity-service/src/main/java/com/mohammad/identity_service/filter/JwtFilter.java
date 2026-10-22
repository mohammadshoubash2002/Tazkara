package com.mohammad.identity_service.filter;

import com.mohammad.identity_service.service.DatabaseUserDetailsService;
import com.mohammad.identity_service.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final DatabaseUserDetailsService databaseUserDetailsService;

    public JwtFilter(JwtService jwtService, DatabaseUserDetailsService databaseUserDetailsService) {
        this.jwtService = jwtService;
        this.databaseUserDetailsService = databaseUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        // Step #1 - check request header
        if(header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step #2 - extract token
        String token = header.substring(7);
        try {
            // Step #3 - validate token
            String username = jwtService.extractUsername(token);
            UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(username);

            // Step #4 - load the user from the database
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }  catch (JwtException | UsernameNotFoundException e) {
            //
        }


        // Step #5 - set the authentication token in the security context
        filterChain.doFilter(request, response);
    }
}
