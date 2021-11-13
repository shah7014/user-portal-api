package com.learningbybuilding.supportportal.service;

import com.learningbybuilding.supportportal.domain.User;
import com.learningbybuilding.supportportal.domain.UserRequest;
import com.learningbybuilding.supportportal.exception.domain.EmailExistException;
import com.learningbybuilding.supportportal.exception.domain.EmailNotFoundException;
import com.learningbybuilding.supportportal.exception.domain.UserNameExistException;
import com.learningbybuilding.supportportal.exception.domain.UserNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    User register(UserRequest userRequest) throws UserNotFoundException, EmailExistException, UserNameExistException;

    User findByEmail(String email);

    List<User> getUsers();

    User findByUserName(String userName);

    User addNewUser(String firstName, String lastName, String userName, String email,
                    String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UserNameExistException, IOException;

    User updateUser(String currentUserName, String newFirstName, String newLastName, String newUserName, String newEmail,
                    String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UserNameExistException, IOException;

    void deleteUser(long id);

    void resetPassword(String email) throws EmailNotFoundException;

    User updateProfileImage(String userName, MultipartFile profileImage) throws UserNotFoundException, IOException;
}
