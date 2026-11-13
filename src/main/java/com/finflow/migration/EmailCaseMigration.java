package com.finflow.migration;

import com.finflow.entity.User;
import com.finflow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * One-off data fix that runs on every startup and is a no-op once the data is clean.
 * <p>
 * Registration and login normalize emails to trimmed lower case, but accounts created
 * before that change may still hold e.g. {@code "Alex@Example.com"}. Login then looks
 * up {@code "alex@example.com"}, finds nothing, and the user is locked out. This rewrites
 * such emails to their canonical form.
 * <p>
 * The project has no Flyway/Liquibase (schema is managed by {@code ddl-auto}), so the fix
 * lives here rather than in a migration script. If the canonical email is already taken
 * by another account (two accounts differing only by case), neither is changed and a
 * warning is logged: merging accounts needs a human decision.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EmailCaseMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EmailCaseMigration.class);

    private final UserRepository userRepository;

    public EmailCaseMigration(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        migrate();
    }

    /**
     * @return how many accounts were rewritten
     */
    @Transactional
    public int migrate() {
        List<User> candidates = userRepository.findWithNonCanonicalEmail();
        if (candidates.isEmpty()) {
            return 0;
        }

        Set<String> claimed = new HashSet<>();
        int fixed = 0;
        for (User user : candidates) {
            String canonical = canonical(user.getEmail());
            if (claimed.contains(canonical) || userRepository.existsByEmail(canonical)) {
                log.warn("Not normalizing email of user id={}: another account already uses its lower-case form. "
                        + "Merge or rename one of the accounts manually.", user.getId());
                continue;
            }
            user.setEmail(canonical);
            userRepository.save(user);
            claimed.add(canonical);
            fixed++;
        }
        log.info("Normalized the email of {} of {} user account(s) to lower case", fixed, candidates.size());
        return fixed;
    }

    static String canonical(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
