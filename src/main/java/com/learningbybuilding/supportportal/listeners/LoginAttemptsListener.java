package com.learningbybuilding.supportportal.listeners;

import com.learningbybuilding.supportportal.cache.LoginAttemptCache;
import com.learningbybuilding.supportportal.domain.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

@Configuration
@Slf4j
public class LoginAttemptsListener {
    @Bean
    public ApplicationListener<AuthenticationFailureBadCredentialsEvent> authenticationFailureBadCredentialsEventApplicationListener(LoginAttemptCache loginAttemptCache) {
        return event -> {
            log.error("failure auth");
            Object principal = event.getAuthentication().getPrincipal();
            String userName;
            if (principal instanceof String) {
                userName = (String) principal;
                loginAttemptCache.addUserToLoginAttemptCache(userName);
            }
        };
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> onReady() {
        return event -> {
            log.info("app is ready");
        };
    }

    @Bean
    public ApplicationListener<AuthenticationSuccessEvent> authenticationSuccessEventApplicationListener(LoginAttemptCache loginAttemptCache) {
        return event -> {
            log.error("success auth");
            Object principal = event.getAuthentication().getPrincipal();
            if (principal instanceof UserPrincipal) {
                String userName =((UserPrincipal) principal).getUsername();
                loginAttemptCache.evictUserFromLoginAttemptCache(userName);
            }
        };
    }
}
