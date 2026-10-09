package com.tynyany.simplewmsv2.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordMigrationTest {

    @Test
    void detectsBcryptHashes() {
        assertThat(PasswordMigration.isBcrypt(new BCryptPasswordEncoder().encode("admin"))).isTrue();
        assertThat(PasswordMigration.isBcrypt("$2a$10$Ht4fAzYGhp/1uGnEzYoBgeRQ56R8SKMo1rbI9oLjJ.D.TQ8jGHROy")).isTrue();
        assertThat(PasswordMigration.isBcrypt("password")).isFalse();
        assertThat(PasswordMigration.isBcrypt("$2a$10$short")).isFalse();
    }
}
