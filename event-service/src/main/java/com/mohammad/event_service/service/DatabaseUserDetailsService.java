package com.mohammad.event_service.service;

import com.mohammad.event_service.dto.UserDetailsServiceStructure;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private UserDetailsServiceStructure user;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return User.withUsername(user.username())
                .password(user.password())
                .roles(user.role())
                .build();
    }

    public void getUserFromIdentityService(String token){
        RestClient restClient = RestClient.create();
        LinkedHashMap<String, Object> response = restClient
                .get()
                .uri("http://localhost:8081/api/users/my-profile/username").header(
                        "Authorization", "Bearer " + token)
                .retrieve()
                .body(LinkedHashMap.class);

        user = new UserDetailsServiceStructure(
                (String) response.get("username"),
                (String) response.get("password"),
                (String) response.get("role")
        );
    }
}
