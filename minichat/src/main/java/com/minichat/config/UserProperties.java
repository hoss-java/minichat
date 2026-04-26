package com.minichat.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProperties {

    private List<UserConfig> admins;
    private List<UserConfig> users;
    private List<UserConfig> moderators;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserConfig {
        private String username;
        private String email;
        private String password;
        private boolean active;
    }
}
