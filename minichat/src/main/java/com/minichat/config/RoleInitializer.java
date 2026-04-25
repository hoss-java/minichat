package com.minichat.config;

import com.minichat.entity.Role;
import com.minichat.entity.RoleType;
import com.minichat.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(1)
@RequiredArgsConstructor
public class RoleInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (RoleType roleType : RoleType.values()) {
            if (roleRepository.findByName(roleType).isEmpty()) {
                Role role = Role.builder()
                        .name(roleType)
                        .description(getDescription(roleType))
                        .createdAt(LocalDateTime.now())
                        .build();
                roleRepository.save(role);
            }
        }
    }

    private String getDescription(RoleType roleType) {
        return switch (roleType) {
            case USER -> "Standard user role";
            case MODERATOR -> "Moderator role with elevated permissions";
            case ADMIN -> "Administrator role with full access";
        };
    }
}


