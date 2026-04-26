package com.minichat.config;

import com.minichat.entity.Role;
import com.minichat.entity.RoleType;
import com.minichat.entity.User;
import com.minichat.repository.RoleRepository;
import com.minichat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Order(3)
@RequiredArgsConstructor
@Profile("dev")
@EnableConfigurationProperties(UserProperties.class)
public class UserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProperties userProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Initialize regular users
        initializeUsersByRole(userProperties.getUsers(), RoleType.USER);
        
        // Initialize moderator users
        initializeUsersByRole(userProperties.getModerators(), RoleType.MODERATOR);
    }

    private void initializeUsersByRole(List<UserProperties.UserConfig> users, RoleType roleType) {

        if (users == null || users.isEmpty()) {
            return;
        }

        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new RuntimeException(roleType + " role not found"));

        for (UserProperties.UserConfig userConfig : users) {
            if (userRepository.findByUsername(userConfig.getUsername()).isEmpty() &&
                userRepository.findByEmail(userConfig.getEmail()).isEmpty()) {
                
                Set<Role> roles = new HashSet<>();
                roles.add(role);

                User user = User.builder()
                        .username(userConfig.getUsername())
                        .email(userConfig.getEmail())
                        .passwordHash(passwordEncoder.encode(userConfig.getPassword()))
                        .isActive(true)
                        .roles(roles)
                        .build();

                userRepository.save(user);
            }
        }
    }
}

