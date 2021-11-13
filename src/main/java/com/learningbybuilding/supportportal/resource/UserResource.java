package com.learningbybuilding.supportportal.resource;

import com.learningbybuilding.supportportal.constant.FileConstants;
import com.learningbybuilding.supportportal.constant.SecurityConstant;
import com.learningbybuilding.supportportal.domain.HttpResponse;
import com.learningbybuilding.supportportal.domain.User;
import com.learningbybuilding.supportportal.domain.UserPrincipal;
import com.learningbybuilding.supportportal.domain.UserRequest;
import com.learningbybuilding.supportportal.exception.domain.EmailExistException;
import com.learningbybuilding.supportportal.exception.domain.EmailNotFoundException;
import com.learningbybuilding.supportportal.exception.domain.UserNameExistException;
import com.learningbybuilding.supportportal.exception.domain.UserNotFoundException;
import com.learningbybuilding.supportportal.service.UserService;
import com.learningbybuilding.supportportal.utility.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserResource {
    public static final String AN_EMAIL_WAS_SENT_TO_S_WITH_THE_NEW_PASSWORD = "An email was sent to %s with the new password";
    public static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully";
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

    @PostMapping("/add")
    public ResponseEntity<User> addNewUser(@RequestParam("firstName") String firstName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("userName") String userName,
                                           @RequestParam("email") String email,
                                           @RequestParam("role") String role,
                                           @RequestParam("isNonLocked") String isNonLocked,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
    ) throws UserNotFoundException, EmailExistException, UserNameExistException, IOException {

        User newUser = userService.addNewUser(firstName, lastName, userName, email, role,
                Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return new ResponseEntity<>(newUser, OK);
    }

    @PostMapping("/update")
    public ResponseEntity<User> update(@RequestParam("currentUserName") String currentUserName,
                                       @RequestParam("firstName") String firstName,
                                       @RequestParam("lastName") String lastName,
                                       @RequestParam("userName") String userName,
                                       @RequestParam("email") String email,
                                       @RequestParam("role") String role,
                                       @RequestParam("isNonLocked") String isNonLocked,
                                       @RequestParam("isActive") String isActive,
                                       @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
    ) throws UserNotFoundException, EmailExistException, UserNameExistException, IOException {

        User updateUser = userService.updateUser(currentUserName, firstName, lastName, userName, email, role,
                Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return new ResponseEntity<>(updateUser, OK);
    }

    @GetMapping("/find/{userName}")
    public ResponseEntity<User> getUser(@PathVariable String userName) {
        User user = userService.findByUserName(userName);
        return new ResponseEntity<>(user, OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getUsers();
        return new ResponseEntity<>(users, OK);
    }

    // don't return void from controller send ResponseEntity with our domain class HttpResponse
    @GetMapping("/resetPassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable String email) throws EmailNotFoundException {
        userService.resetPassword(email);
        return response(OK, String.format(AN_EMAIL_WAS_SENT_TO_S_WITH_THE_NEW_PASSWORD, email));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<HttpResponse> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return response(NO_CONTENT, USER_DELETED_SUCCESSFULLY);
    }

    @PostMapping("/updateProfileImage")
    public ResponseEntity<User> updateProfileImage(@RequestParam("userName") String userName,
                                       @RequestParam(value = "profileImage") MultipartFile profileImage) throws UserNotFoundException, IOException {
        User user = userService.updateProfileImage(userName, profileImage);
        return new ResponseEntity<>(user, OK);
    }

    @GetMapping(path = "/image/{username}/{imageName}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable(value = "username") String userName,
                                  @PathVariable(value = "imageName") String imageName) throws IOException {
        Path imagePath = Paths.get(FileConstants.USER_FOLDER + userName, imageName);
        return Files.readAllBytes(imagePath);
    }

    //read image for robo hash website
    @GetMapping(path = "/image/profile/{userName}", produces = "image/jpeg")
    public byte[] getDefaultProfileImage(@PathVariable String userName) {
    }

    // this can't be used as we don't want to send image as an attachment to be downloaded
    /*@GetMapping("/image/{username}/{imageName}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable(value = "username") String userName,
                                                    @PathVariable(value = "imageName") String imageName) throws IOException {
        Path imagePath = Paths.get(FileConstants.USER_FOLDER + userName, imageName);

        if (!Files.exists(imagePath)) {
            throw new FileNotFoundException(String.format("image not found for username %s", userName));
        }

        Resource resource = new UrlResource(imagePath.toUri());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("filename", imageName);
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + imageName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(Files.probeContentType(imagePath)))
                .headers(httpHeaders)
                .body(resource);
    }*/

    private void authenticate(String userName, String password) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userName, password);
        authenticationManager.authenticate(authentication);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        HttpResponse response = HttpResponse.builder()
                .httpStatus(httpStatus)
                .reason(httpStatus.getReasonPhrase().toUpperCase())
                .httpStatusCode(httpStatus.value())
                .developerMessage(message.toUpperCase())
                .timeStamp(new Date())
                .build();
        return new ResponseEntity<>(response, httpStatus);
    }

}
