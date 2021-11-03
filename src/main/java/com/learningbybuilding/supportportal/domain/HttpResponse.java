package com.learningbybuilding.supportportal.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Builder
@Data
@AllArgsConstructor
public class HttpResponse {
    private int httpStatusCode;
    private HttpStatus httpStatus;
    private String reason;
    private String developerMessage;
    //private Map<?, ?> data;
}
