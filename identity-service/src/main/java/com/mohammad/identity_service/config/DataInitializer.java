package com.mohammad.identity_service.config;

import com.mohammad.identity_service.entity.AppUser;
import com.mohammad.identity_service.entity.Role;
import com.mohammad.identity_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("organizer@tazkara.com")) {
            AppUser organizer = new AppUser.Builder()
                    .username("organizer_demo")
                    .name("Demo Organizer")
                    .email("organizer@tazkara.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .role(Role.ORGANIZER.name())
                    .isOrganizer(true)
                    .organizationName("Tazkara Events Org")
                    .build();
            userRepository.save(organizer);
            log.info("Demo Organizer initialized: organizer@tazkara.com / organizer_demo");
        }

        if (!userRepository.existsByEmail("attendee@tazkara.com")) {
            AppUser attendee = new AppUser.Builder()
                    .username("attendee_demo")
                    .name("Demo Attendee")
                    .email("attendee@tazkara.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .role(Role.ATTENDEE.name())
                    .isOrganizer(false)
                    .organizationName(null)
                    .build();
            userRepository.save(attendee);
            log.info("Demo Attendee initialized: attendee@tazkara.com / attendee_demo");
        }
    }
}
