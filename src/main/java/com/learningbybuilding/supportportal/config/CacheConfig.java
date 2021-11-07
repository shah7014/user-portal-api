package com.learningbybuilding.supportportal.config;

import com.learningbybuilding.supportportal.cache.LoginAttemptCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    @Bean
    public LoginAttemptCache loginAttemptCache() {
        return new LoginAttemptCache(100, 15);
    }
}
