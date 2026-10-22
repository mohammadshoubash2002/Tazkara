package com.mohammad.identity_service.service;

import com.mohammad.identity_service.dto.RegisterRequest;
import com.mohammad.identity_service.entity.AppUser;
import com.mohammad.identity_service.entity.Role;
import com.mohammad.identity_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    public ResponseEntity<Optional<AppUser>> register(RegisterRequest registerRequest) {
        AppUser user = new AppUser.Builder()
                .name(registerRequest.name())
                .email(registerRequest.email())
                .password(encoder.encode(registerRequest.password()))
                .role(Role.ATTENDEE.name())
                .isOrganizer(false)
                .organizationName(null)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(Optional.of(user));
    }
}
