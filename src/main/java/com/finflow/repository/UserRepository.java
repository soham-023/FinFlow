package com.finflow.repository;

import com.finflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Users whose stored email is not in canonical form (trimmed, lower case).
     * Accounts created before emails were normalized can have mixed case and
     * cannot log in, because login looks the email up in lower case.
     */
    @Query("SELECT u FROM User u WHERE u.email <> LOWER(TRIM(u.email)) ORDER BY u.id")
    List<User> findWithNonCanonicalEmail();
}
