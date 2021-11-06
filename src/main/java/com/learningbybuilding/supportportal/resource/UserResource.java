package com.learningbybuilding.supportportal.resource;

import com.learningbybuilding.supportportal.domain.User;
import com.learningbybuilding.supportportal.domain.UserRequest;
import com.learningbybuilding.supportportal.exception.domain.EmailExistException;
import com.learningbybuilding.supportportal.exception.domain.UserNameExistException;
import com.learningbybuilding.supportportal.exception.domain.UserNotFoundException;
import com.learningbybuilding.supportportal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserResource {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> showUser(@RequestBody UserRequest userRequest) throws UserNotFoundException, EmailExistException, UserNameExistException {
        User user = userService.register(userRequest);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
