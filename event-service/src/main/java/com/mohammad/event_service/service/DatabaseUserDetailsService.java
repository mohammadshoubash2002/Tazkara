package com.mohammad.event_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private Object user;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Step 1: find the user in our table
        Object appUser = this.user;

        // Step 2: describe him in Spring Security's language
        return User.withUsername("appUser.username()")
                .password("appUser.password()")
                .roles("appUser.role()")
                .build();
    }

    public Object getUserFromIdentityService(String token){
        // endpoint to get the app user from the identity service
        RestClient restClient = RestClient.create();
        Object response = restClient
                .get()
                .uri("http://localhost:8081/api/users/my-profile").header(
                        "Authorization", "Bearer " + token)
                .retrieve()
                .body(Object.class);

        return response;
    }
}
