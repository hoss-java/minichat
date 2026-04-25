package com.minichat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaRepositories(basePackages = "com.minichat.repository")
@EnableJpaAuditing
public class JpaConfig {
}
