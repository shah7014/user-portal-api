package com.learningbybuilding.supportportal.service;

import com.learningbybuilding.supportportal.cache.LoginAttemptCache;
import com.learningbybuilding.supportportal.domain.User;
import com.learningbybuilding.supportportal.domain.UserPrincipal;
import com.learningbybuilding.supportportal.domain.UserRequest;
import com.learningbybuilding.supportportal.domain.enumeration.UserRole;
import com.learningbybuilding.supportportal.exception.domain.EmailExistException;
import com.learningbybuilding.supportportal.exception.domain.UserNameExistException;
import com.learningbybuilding.supportportal.exception.domain.UserNotFoundException;
import com.learningbybuilding.supportportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    public static final String USER_NAME_ALREADY_EXISTS = "Requested user name already present in records";
    public static final String EMAIL_ALREADY_EXISTS = "Requested email already present in records";
    public static final String NO_USER_BY_USERNAME = "User with %s user name not found in records";
    public static final String DEFAULT_USER_IMAGE_PATH = "/user/image/profile/temp";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptCache loginAttemptCache;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username);
        UserPrincipal userPrincipal;

        if (user == null) {
            LOGGER.error("User not found by user name:- " + username);
            throw new UsernameNotFoundException("User name not found in db:- " + username);
        } else {
            validateIsUserLocked(user);
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());
            userRepository.save(user);
            userPrincipal = new UserPrincipal(user);
            return userPrincipal;
        }
    }

    private void validateIsUserLocked(User user) {
        if (user.isNotLocked()) {
            // validate if max failed attempt reached
            if (loginAttemptCache.hasFailedAttemptsExceededMaxLimit(user.getUserName())) {
                user.setNotLocked(false);
            }
        } else {
            // if the account is locked just remove this user form cache
            loginAttemptCache.evictUserFromLoginAttemptCache(user.getUserName());
        }
    }

    @Override
    public User register(UserRequest userRequest) throws UserNotFoundException, EmailExistException, UserNameExistException {
        validateUserNameAndEmail(StringUtils.EMPTY, userRequest.getUserName(), userRequest.getEmail());
        String userId = generateUserId();
        String password = generatePassword();
        String profileImageUrl = generateTempImageUrl();
        User  newUser = User.builder()
                .userId(userId)
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .userName(userRequest.getUserName())
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(password))
                .joinDate(new Date())
                .profileImageUrl(profileImageUrl)
                .role(UserRole.ROLE_USER.toString())
                .authorities(UserRole.ROLE_USER.getAuthorities())
                .isActive(true)
                .isNotLocked(true)
                .build();
        userRepository.save(newUser);
        LOGGER.info("newly generated user password is:- " + password);
        return newUser;
    }

    private User validateUserNameAndEmail(String currentUserName,
                                          String requestedUserName,
                                          String requestedEmail) throws UserNameExistException, EmailExistException, UserNotFoundException {

        User userForRequestedUserName = findByUserName(requestedUserName);
        User userForRequestedEmail = findByEmail(requestedEmail);

        if (StringUtils.isNotBlank(currentUserName)) {
            User currentUser = findByUserName(currentUserName);

            if (currentUser == null) {
                throw new UserNotFoundException(String.format(NO_USER_BY_USERNAME, currentUserName));
            }

            if (userForRequestedUserName != null && !userForRequestedUserName.getId().equals(currentUser.getId())) {
                throw new UserNameExistException(USER_NAME_ALREADY_EXISTS);
            }

            if (userForRequestedEmail != null && !currentUser.getId().equals(userForRequestedEmail.getId())) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }

            return currentUser;
        } else {

            if (userForRequestedUserName != null) {
                throw new UserNameExistException(USER_NAME_ALREADY_EXISTS);
            }

            if (userForRequestedEmail != null) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return null;
        }
    }

    private String generateTempImageUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH).toUriString();
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }
}
