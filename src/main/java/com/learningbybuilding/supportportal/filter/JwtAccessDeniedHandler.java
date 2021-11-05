package com.learningbybuilding.supportportal.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningbybuilding.supportportal.constant.SecurityConstant;
import com.learningbybuilding.supportportal.domain.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
        HttpResponse httpResponse = HttpResponse.builder()
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .reason(HttpStatus.UNAUTHORIZED.getReasonPhrase().toUpperCase())
                .httpStatusCode(HttpStatus.UNAUTHORIZED.value())
                .developerMessage(SecurityConstant.ACCESS_DENIED_MESSAGE)
                .build();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        ObjectMapper objectMapper = new ObjectMapper();
        ServletOutputStream outputStream = response.getOutputStream();
        objectMapper.writeValue(outputStream, httpResponse);
        outputStream.flush();
    }
}
