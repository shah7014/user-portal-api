package com.learningbybuilding.supportportal.exception;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.learningbybuilding.supportportal.domain.HttpResponse;
import com.learningbybuilding.supportportal.exception.domain.EmailExistException;
import com.learningbybuilding.supportportal.exception.domain.EmailNotFoundException;
import com.learningbybuilding.supportportal.exception.domain.UserNameExistException;
import com.learningbybuilding.supportportal.exception.domain.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Date;

@RestControllerAdvice
@Slf4j
public class ExceptionHandling implements ErrorController {
    public static final String ACCOUNT_LOCKED = "Your account has been locked. Please contact admin";
    public static final String METHOD_IS_NOT_ALLOWED = "This method is not allowed.";
    public static final String INTERNAL_SERVER_ERROR_MSG = "An error occurred while processing the request";
    public static final String INCORRECT_CREDENTIALS = "Username/password incorrect. Please try again";
    public static final String ACCOUNT_DISABLED = "Your account has been disabled. Please contact admin";
    public static final String ERROR_PROCESSING_FILE = "Error occurred while processing file";
    public static final String NOT_ENOUGH_PERMISSION = "You do not have enough permission";

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> accountDisabledException() {
        return createResponseEntity(HttpStatus.BAD_REQUEST, ACCOUNT_DISABLED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> badCredentialsException() {
        return createResponseEntity(HttpStatus.BAD_REQUEST, INCORRECT_CREDENTIALS);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException() {
        return createResponseEntity(HttpStatus.FORBIDDEN, NOT_ENOUGH_PERMISSION);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> lockedException() {
        return createResponseEntity(HttpStatus.UNAUTHORIZED, ACCOUNT_LOCKED);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException tokenExpiredException) {
        return createResponseEntity(HttpStatus.UNAUTHORIZED, tokenExpiredException.getMessage());
    }

    @ExceptionHandler(EmailExistException.class)
    public ResponseEntity<HttpResponse> emailExistException(EmailExistException emailExistException) {
        return createResponseEntity(HttpStatus.BAD_REQUEST, emailExistException.getMessage());
    }

    @ExceptionHandler(UserNameExistException.class)
    public ResponseEntity<HttpResponse> userNameExistException(UserNameExistException exception) {
        return createResponseEntity(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<HttpResponse> emailNotFoundException(EmailNotFoundException exception) {
        return createResponseEntity(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpResponse> userNotFoundException(UserNotFoundException exception) {
        return createResponseEntity(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponse> userNotFoundException(HttpRequestMethodNotSupportedException exception) {
        //String httpMethod = Objects.requireNonNull(exception.getSupportedMethods())[0];
        return createResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, METHOD_IS_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> exception(Exception exception) {
        log.error(exception.getMessage());
        return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG);
    }

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<HttpResponse> noResultException(NoResultException exception) {
        log.error(exception.getMessage());
        return createResponseEntity(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<HttpResponse> ioException(IOException exception) {
        log.error(exception.getMessage());
        return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_PROCESSING_FILE);
    }

    private ResponseEntity<HttpResponse> createResponseEntity(HttpStatus httpStatus, String message) {
        HttpResponse response = HttpResponse.builder()
                .httpStatus(httpStatus)
                .httpStatusCode(httpStatus.value())
                .reason(httpStatus.getReasonPhrase().toUpperCase())
                .developerMessage(message.toUpperCase())
                .timeStamp(new Date())
                .build();
        return new ResponseEntity<>(response, httpStatus);
    }
}
