package com.vdt2025.user_service.configuration;

import com.vdt2025.user_service.entity.Role;
import com.vdt2025.user_service.entity.User;
import com.vdt2025.user_service.repository.RoleRepository;
import com.vdt2025.user_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(
                                        RoleRepository roleRepository,
                                        UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()){
                Role adminRole = new Role();
                adminRole.setName("ADMIN");
                adminRole.setDescription("Administrator role with full access");
                roleRepository.save(adminRole);
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .email("admin@gmail.com")
                        .phone("0123456789")
                        .role(adminRole)
                        .enabled(true)
                        .build();
                userRepository.save(user);
                log.warn("Admin user has been created with default password: admin, please change it");
            }
        };
    }
}
