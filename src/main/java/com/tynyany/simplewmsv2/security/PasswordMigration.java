package com.tynyany.simplewmsv2.security;

import com.tynyany.simplewmsv2.dao.UserEntity;
import com.tynyany.simplewmsv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * При старте хеширует (BCrypt) пароли, сохранённые ранее открытым текстом.
 * До этапа 2 форма пользователей записывала пароль как есть.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordMigration implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int migrated = 0;
        for (UserEntity user : userRepository.findAll()) {
            String pass = user.getPass();
            if (pass != null && !pass.isEmpty() && !isBcrypt(pass)) {
                user.setPass(passwordEncoder.encode(pass));
                userRepository.save(user);
                migrated++;
            }
        }
        if (migrated > 0) {
            log.info("Пароли переведены в BCrypt: {} пользователей", migrated);
        }
    }

    static boolean isBcrypt(String value) {
        return value.matches("^\\$2[aby]?\\$\\d{2}\\$.{53}$");
    }
}
