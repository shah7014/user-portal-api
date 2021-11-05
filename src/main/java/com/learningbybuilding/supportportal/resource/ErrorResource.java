package com.learningbybuilding.supportportal.resource;

import com.learningbybuilding.supportportal.domain.HttpResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class ErrorResource implements ErrorController {
    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> handleError() {
        HttpResponse response =  HttpResponse.builder()
                .timeStamp(new Date())
                .developerMessage("This mapping is not found")
                .httpStatusCode(HttpStatus.NOT_FOUND.value())
                .reason(HttpStatus.NOT_FOUND.getReasonPhrase().toUpperCase())
                .httpStatus(HttpStatus.NOT_FOUND)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
