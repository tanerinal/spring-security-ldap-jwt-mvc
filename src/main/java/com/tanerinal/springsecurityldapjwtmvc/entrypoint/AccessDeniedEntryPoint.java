package com.tanerinal.springsecurityldapjwtmvc.entrypoint;

import com.tanerinal.springsecurityldapjwtmvc.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@Slf4j
public class AccessDeniedEntryPoint implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException {
        log.info("Access Denied. Unauthorized access attempt to resource {} Authorization header: {}",
                httpServletRequest.getRequestURI(),
                httpServletRequest.getHeader(Constants.HEADER_AUTHORIZATION));

        httpServletResponse.sendError(HttpStatus.UNAUTHORIZED.value(),
                "Access Denied. It seems you're trying to access a resource that you are not allowed! ");
    }
}
