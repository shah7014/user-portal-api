package com.learningbybuilding.supportportal.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningbybuilding.supportportal.constant.SecurityConstant;
import com.learningbybuilding.supportportal.domain.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {

        HttpResponse httpResponse = HttpResponse.builder()
                .httpStatus(HttpStatus.FORBIDDEN)
                .reason(HttpStatus.FORBIDDEN.getReasonPhrase().toUpperCase())
                .httpStatusCode(HttpStatus.FORBIDDEN.value())
                .developerMessage(SecurityConstant.FORBIDDEN_MESSAGE + " from entry point")
                .timeStamp(new Date())
                .build();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());

        ObjectMapper objectMapper = new ObjectMapper();
        ServletOutputStream outputStream = response.getOutputStream();
        objectMapper.writeValue(outputStream, httpResponse);
        outputStream.flush();
    }
}
