package com.learningbybuilding.supportportal.resource;

import com.learningbybuilding.supportportal.constant.SecurityConstant;
import com.learningbybuilding.supportportal.domain.User;
import com.learningbybuilding.supportportal.domain.UserPrincipal;
import com.learningbybuilding.supportportal.domain.UserRequest;
import com.learningbybuilding.supportportal.exception.domain.EmailExistException;
import com.learningbybuilding.supportportal.exception.domain.UserNameExistException;
import com.learningbybuilding.supportportal.exception.domain.UserNotFoundException;
import com.learningbybuilding.supportportal.service.UserService;
import com.learningbybuilding.supportportal.utility.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserResource {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserRequest userRequest) throws UserNotFoundException, EmailExistException, UserNameExistException {
        User user = userService.register(userRequest);
        return new ResponseEntity<>(user, OK);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        authenticate(user.getUserName(), user.getPassword());

        User loginUser = userService.findByUserName(user.getUserName());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);

        String token = jwtTokenProvider.generateToken(userPrincipal);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(SecurityConstant.JWT_TOKEN_HEADER, token);

        return new ResponseEntity<>(loginUser, httpHeaders, OK);
    }

    private void authenticate(String userName, String password) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userName, password);
        authenticationManager.authenticate(authentication);
    }
}
