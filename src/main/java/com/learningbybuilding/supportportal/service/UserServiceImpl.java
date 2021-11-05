package com.learningbybuilding.supportportal.service;

import com.learningbybuilding.supportportal.domain.User;
import com.learningbybuilding.supportportal.domain.UserPrincipal;
import com.learningbybuilding.supportportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username);
        UserPrincipal userPrincipal;

        if (user == null) {
            LOGGER.error("User not found by user name:- " + username);
            throw new UsernameNotFoundException("User name not found in db:- " + username);
        } else {
            LOGGER.info("found user in db for user name:- " + username);
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());
            userRepository.save(user);
            userPrincipal = new UserPrincipal(user);
            return userPrincipal;
        }
    }
}
