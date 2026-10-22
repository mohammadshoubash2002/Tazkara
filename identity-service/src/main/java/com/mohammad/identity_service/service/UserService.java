package com.mohammad.identity_service.service;

import com.mohammad.identity_service.dto.RegisterRequest;
import com.mohammad.identity_service.entity.AppUser;
import com.mohammad.identity_service.entity.Role;
import com.mohammad.identity_service.exception.DuplicateEmailException;
import com.mohammad.identity_service.repository.UserRepository;
import com.mohammad.identity_service.util.Validator;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new DuplicateEmailException("Email already in use: " + registerRequest.email());
        }

        // checking email validation
        if (!Validator.isValidEmail(registerRequest.email())) {
            throw new IllegalArgumentException("Invalid email format: " + registerRequest.email());
        }

        // checking password validation
        if (!Validator.isValidPassword(registerRequest.password())) {
            throw new IllegalArgumentException("Invalid password format, must contain at least one lowercase letter, one uppercase letter, one digit, and one special character");
        }

        if(!registerRequest.is_organizer() && registerRequest.organization_name() != null) {
            throw new IllegalArgumentException("You cannot provide an organization name for non-organizer users");
        }

        AppUser user = new AppUser.Builder()
                .name(registerRequest.name())
                .email(registerRequest.email())
                .password(encoder.encode(registerRequest.password()))
                .role(registerRequest.is_organizer() ? Role.ORGANIZER.name() : Role.ATTENDEE.name())
                .isOrganizer(registerRequest.is_organizer())
                .organizationName(registerRequest.organization_name())
                .build();

        userRepository.save(user);

        return new ResponseEntity<>(Optional.of(user), HttpStatus.CREATED);
    }
}
