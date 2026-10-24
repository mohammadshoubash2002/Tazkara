package com.mohammad.booking_service.service;

import com.mohammad.booking_service.dto.UserDetailsServiceStructure;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.LinkedHashMap;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final RestClient restClient;

    public DatabaseUserDetailsService(RestClient.Builder restClientBuilder,
                                      @Value("${identity.service.url:http://localhost:8081}") String identityServiceUrl,
                                      @Value("${event.service.url:http://localhost:8082}") String eventServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(identityServiceUrl).build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UnsupportedOperationException("Load user by username directly is not supported; use loadUserByToken instead.");
    }

    public UserDetails loadUserByToken(String token) {
        LinkedHashMap<String, Object> response = restClient
                .get()
                .uri("/api/users/my-profile/username")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(LinkedHashMap.class);

        if (response == null || !response.containsKey("username")) {
            throw new UsernameNotFoundException("User not found from Identity Service");
        }

        UserDetailsServiceStructure user = new UserDetailsServiceStructure(
                (String) response.get("username"),
                (String) response.get("password"),
                (String) response.get("role")
        );

        return User.withUsername(user.username())
                .password(user.password() != null ? user.password() : "")
                .roles(user.role())
                .build();
    }
}