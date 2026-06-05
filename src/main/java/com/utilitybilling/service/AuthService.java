package com.utilitybilling.service;

import com.utilitybilling.dto.request.LoginRequest;
import com.utilitybilling.dto.request.RegisterRequest;
import com.utilitybilling.dto.response.AuthResponse;
import com.utilitybilling.entity.AppUser;
import com.utilitybilling.entity.Role;
import com.utilitybilling.repository.AppUserRepository;
import com.utilitybilling.repository.RoleRepository;
import com.utilitybilling.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service handling user authentication: registration and login.
 * New registrations default to ROLE_CUSTOMER. Returns JWT tokens upon success.
 */
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(AppUserRepository appUserRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already in use");
        }

        AppUser user = new AppUser();
        user.setFullNames(request.fullNames());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(AppUser.UserStatus.ACTIVE);

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);
        user.setRoles(roles);

        appUserRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtil.generateToken(authentication);

        List<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        return new AuthResponse(token, "Bearer", user.getEmail(), roleNames);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtil.generateToken(authentication);

        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        return new AuthResponse(token, "Bearer", user.getEmail(), roleNames);
    }
}
