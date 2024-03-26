package com.surrealdb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryInitializerAutoConfiguration {

    @Bean
    public static RepositoryInitializer repositoryInitializer() {
        return new RepositoryInitializer();
    }
}
