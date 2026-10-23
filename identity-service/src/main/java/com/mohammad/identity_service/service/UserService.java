package com.mohammad.identity_service.service;

import com.mohammad.identity_service.dto.*;
import com.mohammad.identity_service.entity.AppUser;
import com.mohammad.identity_service.entity.Role;
import com.mohammad.identity_service.exception.DuplicateEmailException;
import com.mohammad.identity_service.exception.ForbiddenException;
import com.mohammad.identity_service.repository.UserRepository;
import com.mohammad.identity_service.util.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

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

        // checking username validation
        if (!Validator.isValidUsername(registerRequest.username())) {
            throw new IllegalArgumentException("Invalid username format, must contain only letters, numbers, underscores, and hyphens, and be between 3 and 20 characters long");
        }

        if(!registerRequest.is_organizer() && registerRequest.organization_name() != null) {
            throw new IllegalArgumentException("You cannot provide an organization name for non-organizer users");
        }

        AppUser user = new AppUser.Builder()
                .username(registerRequest.username())
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

    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        if(loginRequest.email() == null || loginRequest.password() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }

        String userName = userRepository.findByEmail(loginRequest.email()).get().getUsername();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userName, loginRequest.password())
        );

        UserDetails user = (UserDetails) authentication.getPrincipal();

        if(user == null) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new LoginResponse(token, jwtService.getExpirationTimeInMinutes()));
    }

    public ResponseEntity<ProfileResponse> getMyProfile(Principal principal) {
        AppUser appUser = userRepository.findByUsername(principal.getName()).orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(
                new ProfileResponse(
                        "Profile data retrieved successfully", appUser.getUsername(), appUser.getName(), appUser.getEmail(), appUser.getRole(), appUser.getOrganizationName()
                )
        );
    }

    public ResponseEntity<ProfileResponse> updateMyProfile(Principal principal, ProfileEditRequest profileEditRequest) {
        AppUser appUser = userRepository.findByUsername(principal.getName()).orElseThrow(() -> new IllegalArgumentException("User not found"));

        appUser.setName(profileEditRequest.name());

        if(appUser.getIsOrganizer() && profileEditRequest.organization_name() != null){
            appUser.setOrganizationName(profileEditRequest.organization_name());
        } else {
            throw new ForbiddenException("Organization name cannot be updated for non-organizer users");
        }

        appUser.setUsername(profileEditRequest.username());

        // checking username validation
        if (!Validator.isValidUsername(profileEditRequest.username())) {
            throw new IllegalArgumentException("Invalid username format, must contain only letters, numbers, underscores, and be between 3 and 20 characters long");
        }

        userRepository.save(appUser);

        return ResponseEntity.ok(
                new ProfileResponse(
                        "Profile updated successfully", appUser.getUsername(), appUser.getName(), appUser.getEmail(), appUser.getRole(), appUser.getOrganizationName()
                )
        );
    }

    public ResponseEntity<UserDetailsServiceStructure> findByUsername(String username) {
        AppUser user = userRepository.findByUsername(username).get();
        return ResponseEntity.ok(new UserDetailsServiceStructure(user.getUsername(), user.getPassword(), user.getRole()));
    }
}
