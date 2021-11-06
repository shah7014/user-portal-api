package com.learningbybuilding.supportportal.service;

import com.learningbybuilding.supportportal.domain.User;
import com.learningbybuilding.supportportal.domain.UserRequest;
import com.learningbybuilding.supportportal.exception.domain.EmailExistException;
import com.learningbybuilding.supportportal.exception.domain.UserNameExistException;
import com.learningbybuilding.supportportal.exception.domain.UserNotFoundException;

import java.util.List;

public interface UserService {
    User register(UserRequest userRequest) throws UserNotFoundException, EmailExistException, UserNameExistException;

    User findByEmail(String email);

    List<User> getUsers();

    User findByUserName(String userName);
}
