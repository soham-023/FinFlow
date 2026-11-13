package com.finflow.migration;

import com.finflow.dto.AuthRequest;
import com.finflow.entity.User;
import com.finflow.repository.UserRepository;
import com.finflow.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs the migration against the H2 test database, seeding users the way legacy
 * (pre-normalization) registrations stored them.
 */
@SpringBootTest
@ActiveProfiles("test")
class EmailCaseMigrationTest {

    @Autowired
    private EmailCaseMigration migration;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void cleanUsers() {
        userRepository.deleteAll();
    }

    private User legacyUser(String email) {
        return userRepository.save(User.builder()
                .name("Legacy")
                .email(email)
                .passwordHash(passwordEncoder.encode("secret123"))
                .build());
    }

    @Test
    @DisplayName("Mixed-case account is locked out before the migration and can log in after it")
    void mixedCaseAccountCanLogInAfterMigration() {
        legacyUser("Alex@Example.COM");

        assertThatThrownBy(() -> authService.login(new AuthRequest("alex@example.com", "secret123")))
                .isInstanceOf(BadCredentialsException.class);

        assertThat(migration.migrate()).isEqualTo(1);

        assertThat(userRepository.findByEmail("alex@example.com")).isPresent();
        assertThat(authService.login(new AuthRequest("Alex@Example.COM", "secret123")).email())
                .isEqualTo("alex@example.com");
    }

    @Test
    @DisplayName("Surrounding whitespace is trimmed too")
    void trimsWhitespace() {
        legacyUser("  sam@example.com ");

        assertThat(migration.migrate()).isEqualTo(1);
        assertThat(userRepository.findByEmail("sam@example.com")).isPresent();
    }

    @Test
    @DisplayName("Already-canonical emails are left alone and a second run is a no-op")
    void idempotent() {
        legacyUser("ok@example.com");
        legacyUser("Mixed@Example.com");

        assertThat(migration.migrate()).isEqualTo(1);
        assertThat(migration.migrate()).isZero();
        assertThat(userRepository.findAll()).extracting(User::getEmail)
                .containsExactlyInAnyOrder("ok@example.com", "mixed@example.com");
    }

    @Test
    @DisplayName("Accounts that collide on case are not merged or overwritten")
    void collisionsAreSkipped() {
        legacyUser("dup@example.com");
        legacyUser("Dup@Example.com");
        legacyUser("Twin@Example.com");
        legacyUser("TWIN@example.com");

        // Only the first of the two "twin" accounts can take the canonical address
        assertThat(migration.migrate()).isEqualTo(1);
        assertThat(userRepository.findAll()).extracting(User::getEmail)
                .containsExactlyInAnyOrder("dup@example.com", "Dup@Example.com", "twin@example.com", "TWIN@example.com");
    }
}
