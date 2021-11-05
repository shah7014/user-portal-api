package com.learningbybuilding.supportportal.resource;

import com.learningbybuilding.supportportal.exception.domain.EmailExistException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserResource {
    @GetMapping("/home")
    public String showUser() throws EmailExistException {
        return "app works";
    }
}
