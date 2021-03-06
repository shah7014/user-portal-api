package com.learningbybuilding.supportportal.service;

import com.learningbybuilding.supportportal.cache.LoginAttemptCache;
import com.learningbybuilding.supportportal.constant.FileConstants;
import com.learningbybuilding.supportportal.domain.User;
import com.learningbybuilding.supportportal.domain.UserPrincipal;
import com.learningbybuilding.supportportal.domain.UserRequest;
import com.learningbybuilding.supportportal.domain.enumeration.UserRole;
import com.learningbybuilding.supportportal.exception.domain.EmailExistException;
import com.learningbybuilding.supportportal.exception.domain.EmailNotFoundException;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static com.learningbybuilding.supportportal.constant.FileConstants.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    public static final String USER_NAME_ALREADY_EXISTS = "Requested user name already present in records";
    public static final String EMAIL_ALREADY_EXISTS = "Requested email already present in records";
    public static final String NO_USER_BY_USERNAME = "User with %s user name not found in records";
    private static final String NO_USER_FOUND_BY_EMAIL = "no user found for email %s";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptCache loginAttemptCache;
    private final EmailService emailService;

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
        String profileImageUrl = generateTempImageUrl(userRequest.getUserName());
        User newUser = User.builder()
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
        // send password via email for new user
        emailService.sendEmail(newUser.getFirstName(), password, newUser.getEmail());
        LOGGER.info("newly generated user password is:- " + password);
        userRepository.save(newUser);
        return newUser;
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

    @Override
    public User addNewUser(String firstName, String lastName, String userName, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UserNameExistException, IOException {
        validateUserNameAndEmail(null, userName, email);
        String password = generatePassword();
        String userId = generateUserId();
        String profileImageUrl = generateTempImageUrl(userName);
        User newUser = User.builder()
                .userId(userId)
                .firstName(firstName)
                .lastName(lastName)
                .userName(userName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .joinDate(new Date())
                .profileImageUrl(profileImageUrl)
                .role(getRole(role).toString())
                .authorities(getRole(role).getAuthorities())
                .isActive(isActive)
                .isNotLocked(isNonLocked)
                .build();
        //don't save profile image if anything fails before it so saving temp image first and replace it later
        userRepository.save(newUser);
        saveProfileImage(newUser, profileImage);
        LOGGER.info("newly generated password for user {} is {}", userName, password);
        return newUser;
    }

    @Override
    public User updateUser(String currentUserName, String newFirstName, String newLastName, String newUserName, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UserNameExistException, IOException {
        User currentUser = validateUserNameAndEmail(currentUserName, newUserName, newEmail);
        currentUser.setFirstName(newFirstName);
        currentUser.setLastName(newLastName);
        currentUser.setUserName(newUserName);
        currentUser.setEmail(newEmail);
        currentUser.setActive(isActive);
        currentUser.setNotLocked(isNonLocked);
        currentUser.setRole(getRole(role).toString());
        currentUser.setAuthorities(getRole(role).getAuthorities());

        userRepository.save(currentUser);
        saveProfileImage(currentUser, profileImage);
        return currentUser;
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void resetPassword(String email) throws EmailNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new EmailNotFoundException(String.format(NO_USER_FOUND_BY_EMAIL, email));
        }
        String password = generatePassword();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        emailService.sendEmail(user.getFirstName(), password, email);
    }

    @Override
    public User updateProfileImage(String userName, MultipartFile profileImage) throws UserNotFoundException, IOException {
        User user = userRepository.findByEmail(userName);
        if (user == null) {
            throw new UserNotFoundException(String.format(NO_USER_BY_USERNAME, userName));
        }
        saveProfileImage(user, profileImage);
        return user;
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

    private String generateTempImageUrl(String userName) {
        //TODO we need to use robots url here but we will do this in our controller mapped with this mapping
        // and them go to robots url from our controller
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstants.DEFAULT_USER_IMAGE_PATH + userName).toUriString();
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
        if (profileImage != null) {

            Path userDirectory = Paths.get(USER_FOLDER + user.getUserName()).toAbsolutePath().normalize();

            if (!Files.exists(userDirectory)) {
                Files.createDirectories(userDirectory);
                LOGGER.info(DIRECTORY_CREATED);
            }

            Path imagePath = userDirectory.resolve(user.getUserName() + DOT + JPG_EXTENSION);
            Files.copy(profileImage.getInputStream(), imagePath, REPLACE_EXISTING);

            // TODO can we use .path instead of forward slash??
            String imageUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath().path(USER_IMAGE_PATH + user.getUserName() + FORWARD_SLASH + user.getUserName() + DOT + JPG_EXTENSION).toUriString();
            user.setProfileImageUrl(imageUrl);
            userRepository.save(user);

        }
    }

    private UserRole getRole(String role) {
        return UserRole.valueOf(role.toUpperCase());
    }
}
