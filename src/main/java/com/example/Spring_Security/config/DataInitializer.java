package com.example.Spring_Security.config;

import com.example.Spring_Security.model.Role;
import com.example.Spring_Security.model.User;
import com.example.Spring_Security.repository.RoleRepository;
import com.example.Spring_Security.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role userRole = createRoleIfNotExists("USER");
        Role adminRole = createRoleIfNotExists("ADMIN");

        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("Admin");
            admin.setPhone("+1234567890");
            admin.setRoles(new HashSet<>());
            admin.getRoles().add(adminRole);
            admin.getRoles().add(userRole); // Админ также имеет роль пользователя
            userRepository.save(admin);
        }
    }

    private Role createRoleIfNotExists(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    return roleRepository.save(role);
                });
    }
}