package com.learningbybuilding.supportportal;

import com.learningbybuilding.supportportal.cache.LoginAttemptCache;
import com.learningbybuilding.supportportal.domain.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

@SpringBootApplication
@Slf4j
public class SupportPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupportPortalApplication.class, args);
	}
}
