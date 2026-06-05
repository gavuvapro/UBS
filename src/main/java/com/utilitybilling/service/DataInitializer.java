package com.utilitybilling.service;

import com.utilitybilling.entity.AppUser;
import com.utilitybilling.entity.Role;
import com.utilitybilling.repository.AppUserRepository;
import com.utilitybilling.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Seed data initializer that runs on application startup.
 * Ensures all four system roles exist and creates a default ADMIN user for initial access.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, AppUserRepository appUserRepository,
                          PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = createRoleIfNotExists("ROLE_ADMIN");
        Role operatorRole = createRoleIfNotExists("ROLE_OPERATOR");
        Role financeRole = createRoleIfNotExists("ROLE_FINANCE");
        Role customerRole = createRoleIfNotExists("ROLE_CUSTOMER");

        if (!appUserRepository.existsByEmail("admin@wasac.rw")) {
            AppUser admin = new AppUser();
            admin.setFullNames("System Administrator");
            admin.setEmail("admin@wasac.rw");
            admin.setPhoneNumber("+250700000001");
            admin.setPassword(passwordEncoder.encode("Admin123"));
            admin.setStatus(AppUser.UserStatus.ACTIVE);

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(operatorRole);
            roles.add(financeRole);
            admin.setRoles(roles);

            appUserRepository.save(admin);
        }
    }

    private Role createRoleIfNotExists(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }
}
