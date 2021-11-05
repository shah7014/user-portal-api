package com.learningbybuilding.supportportal.resource;

import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserResource {
    @GetMapping("/home")
    public String showUser() throws DisabledException{
        return "app works";
    }
}
