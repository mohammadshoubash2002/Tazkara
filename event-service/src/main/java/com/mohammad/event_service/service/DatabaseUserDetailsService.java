package com.mohammad.event_service.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Step 1: find the user in our table
        Object appUser = new RestTemplate().getForObject("http://localhost:8081/api/users/", Object.class);

        // Step 2: describe him in Spring Security's language
//        return User.withUsername(appUser.getEmail())
//                .password(appUser.getPassword())
//                .roles(appUser.getRole())
//                .build();

        return User.withUsername("")
                .password("")
                .roles("")
                .build();
    }
}
