package com.learningbybuilding.supportportal.config;

import com.learningbybuilding.supportportal.constant.SecurityConstant;
import com.learningbybuilding.supportportal.filter.JwtAuthorizationFilter;
import com.learningbybuilding.supportportal.utility.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .authenticationEventPublisher(authenticationEventPublisher())
                .userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    // need to have this bean and set it up with authentication builder otherwise u will not be able
    // to listen to success or failure authentication
    @Bean
    public DefaultAuthenticationEventPublisher authenticationEventPublisher() {
        return new DefaultAuthenticationEventPublisher();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .cors()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(SecurityConstant.PUBLIC_URLS).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new JwtAuthorizationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authenticationEntryPoint);
    }

    @Override
    @Bean
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }
}
