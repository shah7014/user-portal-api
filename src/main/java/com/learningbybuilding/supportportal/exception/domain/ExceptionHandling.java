package com.learningbybuilding.supportportal.exception.domain;

import com.learningbybuilding.supportportal.domain.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Locale;

@RestControllerAdvice
public class ExceptionHandling {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandling.class);
    public static final String ACCOUNT_LOCKED = "Your account has been locked. Please contact admin";
    public static final String METHOD_IS_NOT_ALLOWED = "This method is not allowed. Please send a %s request";
    public static final String INTERNAL_SERVER_ERROR_MSG = "An error occurred while processing the request";
    public static final String INCORRECT_CREDENTIALS = "Username/password incorrect. Please try again";
    public static final String ACCOUNT_DISABLED = "Your account has been disabled. Please contact admin";
    public static final String ERROR_PROCESSING_FILE = "Error occurred while processing file";
    public static final String NOT_ENOUGH_PERMISSION = "You do not have enough permission";

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> accountDisabledException() {
        return createHttpResponse(HttpStatus.BAD_REQUEST, ACCOUNT_DISABLED);
    }


    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message) {
        HttpResponse httpResponse = HttpResponse.builder()
                .httpStatus(httpStatus)
                .httpStatusCode(httpStatus.value())
                .reason(httpStatus.getReasonPhrase().toUpperCase())
                .developerMessage(message.toUpperCase())
                .build();
        return new ResponseEntity<>(httpResponse, httpStatus);
    }
}
